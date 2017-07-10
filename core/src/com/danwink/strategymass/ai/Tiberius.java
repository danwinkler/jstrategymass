package com.danwink.strategymass.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.ai.MapAnalysis.Zone;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Player;
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
	public float zoneIsBorderBuff = 50;
	public float neighborUnitsScalar = 1;
	public float neighborCountScalar = 6;
	public float distanceFromHomeBaseScalar = 1;
	public float emptyZoneBuff = 50;
	
	public float currentZoneBuff = 30;
	public float distanceToZoneScalar = 1;
	public float visibleEnemyScalar = 1;
	
	MapAnalysis ma;
	AIAPI api;
	
	StateMachine<Tiberius, AIState> sm;
	
	LinkedList<BattleGroup> groups = new LinkedList<>();
	HashMap<Unit, BattleGroup> unitGroupMap = new HashMap<>();
	
	ArrayList<ZoneScore> globalScores = new ArrayList<>();
	
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
			
			//Initialize globalScores store
			ma.zones.forEach( z -> globalScores.add( new ZoneScore( z ) ) );
			
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
							(locationGrid.x*.5f) * b.c.state.map.tileWidth, 
							(locationGrid.x*.5f) * b.c.state.map.tileHeight 
						);
						
						b.moveUnit( u, location.x, location.y );
					}
				}	
			}
		},
		MAIN() {
			public void update( Tiberius b )
			{
				//For each unit
					//If not in group
						//Look for nearby group
						//If found
							//add
						//else
							//create new group and add
				
				//Create Global Zone rankings
				
				//For each group
					//update
					//if close to another, add all our units to theirs
					//if # of units == 0
						//remove group
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
			int unitsInZone = api.numUnitsInZone( z, ma );
			
			// + How many of our own units are there;
			score += api.numUnitsInZone( z, ma, u -> u.team == c.me.team ) * ownUnitsInZoneScalar;
			// + If the zone is on the border
			if( isBorder ) score += zoneIsBorderBuff;
			// + Neighboring zones have enemy units
			score += neighborUnits * neighborUnitsScalar;
			// - Number of neighbors
			score -= z.neighbors.size() * neighborCountScalar;
			// - Distance from home base
			score -= maxDistanceFromBase * distanceFromHomeBaseScalar;
			// + Zone is empty
			if( unitsInZone == 0 ) score += unitsInZone;
			
			s.score = score;
		});
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
		
		public void update( float dt )
		{
			//Remove dead units
			for( int i = 0; i < units.size(); i++ )
			{
				if( units.get( i ).remove )
				{
					unitGroupMap.remove( units.remove( i ) );
					i--;
				}
			}
			
			//Zone scores are the combination of group specific scores and global scores
			calculateZoneScores();
			
			ZoneScore max = zoneScores.stream().max( (a, b) -> Float.compare( a.score, b.score ) ).get();
			Zone currentZone = ma.getZone( location.x, location.y );
			
			if( currentZone != max.z )
			{
				move( max.z );
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
				
				// + Currently in zone
				if( currentZone == z ) score += currentZoneBuff;
				// - Distance to point
				score -= distanceToPoint * distanceToZoneScalar;
				// - zone is visible to lots of enemies
				score -= visibleUnits * visibleEnemyScalar;
				
				s.score = score + globalScores.get( i ).score;
			}
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
}
