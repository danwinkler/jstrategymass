package com.danwink.strategymass.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.ai.MapAnalysis.Neighbor;
import com.danwink.strategymass.ai.MapAnalysis.Zone;
import com.danwink.strategymass.ai.SectorAI.Army;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Point;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.strategymass.game.objects.UnitWrapper;
import com.danwink.strategymass.nethelpers.ClientMessages;
import com.danwink.strategymass.nethelpers.Packets;
/**
 * Every tick, rank all owned zones by how much we want to reinforce it
 * 
 * When units are built, they are added to a temp group, and then move to the #1 reinforce zone
 * When two armies are next to each other, they merge.
 * 
 * Global Reinforce score function
 * - How many of our own units are there
 * + Is a border zone
 * + zone neighbors zone with lots of enemies
 * + The fewer neighbors a zone has, the better
 * - Distance from home base (normalized by max)
 * + Zone is empty
 * + Zone is next to zone owned by strongest team
 * 
 * Group specific reinforce score function
 * + Zones you are currently in get a point bonus (to dissuade moving around)
 * - Distance to point
 * - zone is visible to lots of enemies
 * 
 * Groups
 * @author dan
 *
 */
public class Tiberius extends Bot
{
	public float ownUnitsInZoneScalar = 1;
	public float zoneIsBorderBuff = 150;
	public float neighborUnitsScalar = .9f;
	public float neighborCountScalar = 6;
	public float distanceFromHomeBaseScalar = 1;
	public float emptyZoneBuff = 150;
	public float strongestTeamBuff = 25;
	
	public float currentZoneBuff = 30;
	public float distanceToZoneScalar = 1;
	public float visibleEnemyScalar = 1;
	
	public float attackStrengthRatio = .75f;
	
	MapAnalysis ma;
	AIAPI api;
	
	StateMachine<Tiberius, AIState> sm;
	
	LinkedList<BattleGroup> groups = new LinkedList<>();
	HashMap<Integer, BattleGroup> unitGroupMap = new HashMap<>();
	
	ArrayList<ZoneScore> globalScores = new ArrayList<>();
	ArrayList<TeamScore> teamScores = new ArrayList<>();
	
	int numAllies;
	int playerTeamIndex;
	

	public void reset()
	{
		
	}

	public void update( Player me, GameState state, float dt )
	{
		if( state.map == null || me == null ) return;
		
		if( ma == null )
		{
			ma = new MapAnalysis();
			ma.build( state.map );
			
			api = new AIAPI( c );
			sm = new DefaultStateMachine<>( this, AIState.EXPAND );
			
			//Initialize globalScores, teamScore
			ma.zones.forEach( z -> globalScores.add( new ZoneScore( z ) ) );
			IntStream.range( 0, 4 ).forEach( t -> teamScores.add( new TeamScore( t ) ) );
			
			//Calculate # of allies and what team we are on
			for( int i = 0; i < c.state.players.size(); i++ )
			{
				Player p = c.state.players.get( i );
				if( p.syncId == c.me.syncId )
				{
					playerTeamIndex = numAllies;
				}
				if( p.team == c.me.team )
				{
					numAllies++;
				}
			}
		}
		
		//Build units if we have enough money
		while( me.money >= Unit.unitCost )
		{
			send( ClientMessages.BUILDUNIT, null );
			me.money -= Unit.unitCost;
		}
		
		sm.update();
	}
	
	public enum AIState implements State<Tiberius>
	{
		EXPAND() {
			public void update( Tiberius b )
			{
				for( int i = 0; i < b.c.state.units.size(); i++ )
				{
					UnitWrapper uw = b.c.state.units.get( i );
					Unit u = uw.getUnit();
					
					//If unit isn't moving
					if( u.onPath == -1 )
					{
						//Find zone
						Zone next = b.nextExpandZone( u );
						
						//If no zone, go to main phase
						if( next == null )
						{
							b.sm.changeState( MAIN );
							return;
						}
						
						//Junk to move unit
						GridPoint2 locationGrid = next.p.randomAdjacent( b.c.state.map );
						Vector2 location = new Vector2( 
							(locationGrid.x+.5f) * b.c.state.map.tileWidth, 
							(locationGrid.y+.5f) * b.c.state.map.tileHeight 
						);
						
						b.moveUnit( u, location.x, location.y );
					}
				}	
			}
		},
		MAIN() {
			public void update( Tiberius b )
			{
				//Add units to groups
				b.c.state.units.forEach( uw -> {
					Unit u = uw.getUnit();
					if( !b.unitGroupMap.containsKey( u.syncId ) )
					{
						BattleGroup myGroup = null;
						for( BattleGroup g : b.groups )
						{
							if( g.location.dst( u.pos ) < b.c.state.map.tileWidth*5 )
							{
								myGroup = g;
								break;
							}
						}
						
						//If we still didnt find an army to add to, start a new one
						if( myGroup == null )
						{
							myGroup = b.new BattleGroup();
							myGroup.location = new Vector2( u.pos.x, u.pos.y );
							b.groups.add( myGroup );
						}
						
						myGroup.units.add( u );
						b.unitGroupMap.put( u.syncId, myGroup );
					}
				});
				
				//Create Global Zone rankings
				b.calculateGlobalScores();
				b.calculateTeamScores();
				
				Iterator<BattleGroup> groupIter = b.groups.iterator();
				while( groupIter.hasNext() )
				{
					BattleGroup g = groupIter.next();
					
					//Update
					g.update();
					
					//Combine groups
					if( !g.isMoving() )
					{
						for( BattleGroup og : b.groups )
						{
							if( g == og ) continue;
							if( og.isMoving() ) continue;
							if( g.units.size() + og.units.size() > 100 ) continue; //TODO: Move var to top
							
							if( og.location.dst( g.location ) < b.c.state.map.tileWidth*5 )
							{
								og.units.addAll( g.units );
								for( Unit u : g.units )
								{
									b.unitGroupMap.put( u.syncId, og );
								}
								g.units.clear();
								break;
							}
						}
					}
					
					//Remove group if it doesn't have any units
					if( g.units.size() == 0 )
					{
						groupIter.remove();
					}
				}
			}
		};
		
		public void enter( Tiberius b ) {}
		
		public void exit( Tiberius b ) {}

		public boolean onMessage( Tiberius b, Telegram m )
		{
			return false;
		}
	}
	
	public Zone nextExpandZone( Unit u )
	{
		Zone z = ma.getZone( u.pos.x, u.pos.y );
		//Get unclaimed neighbors and sort by distance
		List<Zone> untaken = z.neighbors.stream()
			.filter( n -> n.z.p.team == -1 ) //Untaken points
			.sorted( (a, b) -> a.distance - b.distance ) //Sort by distance
			.map( n -> n.z ) //Get zone
			.collect( Collectors.toList() );
		
		//If we don't have any untaken neighbors, find the closest untaken point to base
		if( untaken.size() == 0 )
		{
			untaken = ma.zones.stream()
				.filter( zone -> zone.p.team == -1 )
				.sorted( (a, b) -> a.baseDistances[c.me.team] - b.baseDistances[c.me.team] )
				.collect( Collectors.toList() );
		}
		
		//If there aren't any untaken points, return null
		if( untaken.size() == 0 )
		{
			return null;
		}
		
		//Go to the n-th one, where n is our team index (so everyone goes to a different one)
		return untaken.get( playerTeamIndex % untaken.size() );	
	}
	
	public void calculateGlobalScores()
	{
		globalScores.forEach( s -> {
			Zone z = s.z;
			float score = 0;
			
			//Precompute some stuff
			boolean isBorder = api.isBorder( z );
			int neighborUnits = z.neighbors.stream()
				.mapToInt( n -> numUnitsInZone( n.z, ma, u -> u.team != c.me.team ) )
				.sum();
			float maxDistanceFromBase = ma.zones.stream()
				.max( (a, b) -> a.baseDistances[c.me.team] - b.baseDistances[c.me.team] )
				.get()
				.baseDistances[c.me.team];
			float strongestTeamNeighbors = z.neighbors.stream()
				.filter( n -> n.z.p.team == teamScores.get( 0 ).team )
				.mapToInt( n -> 1 )
				.sum();
			
			// + If the zone is on the border
			if( isBorder ) score += zoneIsBorderBuff;
			// + Neighboring zones have enemy units
			score += neighborUnits * neighborUnitsScalar;
			// - Number of neighbors
			score -= z.neighbors.size() * neighborCountScalar;
			// - Distance from home base
			score -= (z.baseDistances[c.me.team] * distanceFromHomeBaseScalar) / maxDistanceFromBase;
			// + Zone is next to zone owned by strongest team
			score += strongestTeamNeighbors * strongestTeamBuff;
			// -- We don't own zone
			if( z.p.team != c.me.team ) score -= 1000;
			
			s.score = score;
		});
	}
	
	public void calculateTeamScores()
	{
		teamScores.forEach( t -> t.strength = 0 );
		
		teamScores.sort( (a, b) -> a.team - b.team );
		
		c.state.units.forEach( u -> {
			teamScores.get( c.team ).strength++;
		});
		
		//Largest to smallest
		teamScores.sort( (a, b) -> b.strength - a.strength );
	}
	
	public class BattleGroup
	{
		LinkedList<Unit> units = new LinkedList<>();
		Vector2 location = new Vector2();
		ArrayList<ZoneScore> zoneScores = new ArrayList<>();
		
		public BattleGroup()
		{
			ma.zones.forEach( z -> zoneScores.add( new ZoneScore( z ) ) );
		}
		
		public void update()
		{
			//Remove dead units
			for( int i = 0; i < units.size(); i++ )
			{
				if( units.get( i ).remove )
				{
					unitGroupMap.remove( units.remove( i ).syncId );
					i--;
				}
			}
			
			Zone currentZone = ma.getZone( location.x, location.y );
			
			//If we are taking another zone, don't do anything else
			if( currentZone.p.team != c.me.team )
			{
				//If we are at a non-capturable base, attack
				if( !currentZone.p.isCapturable( c.state ) )
				{
					attack();
				}
				
				return;
			}
			
			//Zone scores are the combination of group specific scores and global scores
			calculateZoneScores();
			
			ZoneScore max = zoneScores.stream().max( (a, b) -> Float.compare( a.score, b.score ) ).get();
			
			if( currentZone != max.z )
			{
				move( max.z );
				return;
			}
			
			attack();
		}
		
		public void attack()
		{
			if( isMoving() ) return;
			
			Zone attackZone = getAttackZone();
			if( attackZone != null )
			{
				move( attackZone );
				return;
			}
			
			//Finally a check to make sure we don't get stuck
			int ourSize = 0, theirSize = 0;
			for( UnitWrapper uw : c.state.units )
			{
				Unit u = uw.getUnit();
				if( c.state.playerMap.get( u.owner ).team == c.me.team )
				{
					ourSize++;
				}
				else
				{
					theirSize++;
				}
			}
			
			if( ourSize > theirSize * 2.5f )
			{
				for( Zone z : ma.zones )
				{
					if( z.p.team != c.me.team && z.p.isCapturable( c.state ) )
					{
						move( z );
						return;
					}
				}
			}
		}
		
		public void move( Zone z )
		{
			GridPoint2 locGrid = z.p.randomAdjacent( c.state.map );
			location.x = (locGrid.x + .5f) * c.state.map.tileWidth;
			location.y = (locGrid.y + .5f) * c.state.map.tileHeight;
			
			ArrayList<Integer> unitIdsToMove = new ArrayList<Integer>();
			for( Unit u : units )
			{
				unitIdsToMove.add( u.syncId );
			}
			send( ClientMessages.MOVEUNITS, new Packets.MoveUnitPacket( location, unitIdsToMove ) );
		}
		
		public boolean isMoving()
		{
			for( Unit u : units )
			{
				if( u.isMoving() )
				{
					return true;
				}
			}
			return false;
		}
		
		public void calculateZoneScores()
		{
			GridPoint2 currentTile = c.state.map.worldToTile( location.x, location.y );
			Zone currentZone = ma.getZone( location.x, location.y );
			
			for( int i = 0; i < zoneScores.size(); i++ )
			{
				ZoneScore s = zoneScores.get( i );
				Zone z = s.z;
				float score = 0;
				
				//Precompute some stuff
				int visibleUnits = z.visible.stream()
					.mapToInt( v -> numUnitsInZone( v, ma, u -> u.team != c.me.team ) )
					.sum();
				int distanceToPoint = ma.graph.search( currentTile.x, currentTile.y, z.adjacent.x, z.adjacent.y ).size();
				int unitsInZone = api.numUnitsInZone( z, ma, u -> u.team == c.me.team && unitGroupMap.get( u.syncId ) != this );
				
				// + Currently in zone
				if( currentZone == z ) score += currentZoneBuff;
				// - Distance to point
				score -= distanceToPoint * distanceToZoneScalar;
				// - zone is visible to lots of enemies
				score -= visibleUnits * visibleEnemyScalar;
				// + Zone is empty
				if( unitsInZone == 0 ) score += unitsInZone;
				// - How many of our own units are there;
				score -= api.numUnitsInZone( z, ma, u -> u.team == c.me.team && unitGroupMap.get( u.syncId ) != this ) * ownUnitsInZoneScalar;
				
				s.score = score + globalScores.get( i ).score;
			}
		}
		
		public Zone getAttackZone()
		{
			Zone currentZone = ma.getZone( location.x, location.y );
			for( Neighbor n : currentZone.neighbors )
			{
				if( n.z.p.team == c.me.team ) continue;
				
				if( !n.z.p.isCapturable( c.state ) ) continue;
				
				int enemyUnits = api.numUnitsInZone( n.z, ma, u -> u.team != c.me.team );
				
				if( units.size() * attackStrengthRatio > enemyUnits )
				{
					return n.z;
				}
			}
			
			return null;
		}
	}
	
	public class ZoneScore
	{
		float score;
		Zone z;
		
		public ZoneScore( Zone z )
		{
			this.z = z;
		}
	}
	
	public class TeamScore
	{
		int strength;
		int team;
		
		public TeamScore( int team )
		{
			this.team = team;
		}
	}
}
