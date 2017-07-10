package com.danwink.strategymass.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.ai.MapAnalysis.Neighbor;
import com.danwink.strategymass.ai.MapAnalysis.Zone;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Point;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.strategymass.game.objects.UnitWrapper;
import com.danwink.strategymass.nethelpers.ClientMessages;
import com.danwink.strategymass.nethelpers.Packets;

//Augustus
public class SectorAI extends Bot
{	
	MapAnalysis la;
	boolean battlePhase = false;
	ArrayList<Army> armies = new ArrayList<>();
	HashMap<Unit, Army> unitArmyMap = new HashMap<>();
	Point homeBase;
	float expandDelay;
	
	public void update( Player me, GameState state, float dt )
	{
		if( state.map == null || me == null ) return;
		
		if( la == null )
		{
			la = new MapAnalysis();
			la.build( state.map );
			
			//Find Home Base
			for( Point b : state.map.points )
			{
				if( b.isBase && b.team == me.team )
				{
					homeBase = b;
					break;
				}
			}
		}
		assert( homeBase != null );
		if( !battlePhase ) expandPhase( me, state, dt );
		else battlePhase( me, state );
		
	}
	
	public void reset()
	{
		la = null;
		battlePhase = false;
		armies.clear();
		unitArmyMap.clear();
		homeBase = null;
		expandDelay = MathUtils.random( 0, 1 );
	}
	
	//In this phase we try to expand and take every untaken point
	public void expandPhase( Player me, GameState state, float dt )
	{
		if( expandDelay > 0 ) 
		{
			expandDelay -= dt;
			return;
		}
		
		//Build
		if( me.money >= Unit.unitCost )
		{
			send( ClientMessages.BUILDUNIT, null );
		}
		
		//If any unit is not moving, send it to the closest untaken point
		for( UnitWrapper uw : state.units )
		{
			Unit u = uw.getUnit();
			if( u.owner != me.playerId ) continue;
			
			if( !u.isMoving() ) 
			{
				//Find adjacent point noone's headed to
				Point b = getBestUntakenAdjacentPoint( u, state, me.team ); 
						
				//otherwise find closest point no ones headed to
				if( b == null )
				{
					b = findPointShortestPath( u.pos.x, u.pos.y, la.graph, tb -> { 
						if( tb.team != -1 ) return false;
						
						//Look at other units and see if a unit is already heading there
						for( UnitWrapper ouw : state.units )
						{
							Unit ou = ouw.getUnit();
							if( ou.syncId == u.syncId ) continue;
							
							if( ou.onPath >= 0 && la.getZone( ou.targetX, ou.targetY ).p.id == tb.id )
							{
								return false;
							}
						}
						return true;
					});
				}
				
				//Finally, if units are headed towards all untaken points, just double up
				if( b == null )
				{
					b = findPointShortestPath( u.pos.x, u.pos.y, la.graph, tb -> tb.team == -1 );
				}

				//If all points are taken, head to battlePhase
				if( b == null )
				{
					battlePhase = true;
					return;
				}
				
				GridPoint2 adj = b.randomAdjacent( state.map );
				moveUnit( u, (adj.x+.5f)*state.map.tileWidth, (adj.y+.5f)*state.map.tileHeight );
			}
		}
	}
	
	public Point getBestUntakenAdjacentPoint( Unit u, GameState state, int team )
	{
		Zone z = la.getZone( u.pos.x, u.pos.y );
		Point best = null;
		int distance = 100000;
		neighborLoop:
		for( Neighbor n : z.neighbors )
		{
			if( n.z.p.team != -1 ) continue;
			
			for( UnitWrapper uw : state.units )
			{
				Unit ou = uw.getUnit();
				if( ou.syncId == u.syncId ) continue;
				if( ou.onPath < 0 ) continue;
				
				Zone oz = la.getZone( ou.targetX, ou.targetY );
				
				if( oz == n.z )
				{
					continue neighborLoop;
				}
			}
			
			int total = 0;
			for( int i = 0; i < n.z.baseDistances.length; i++ ) { if( team != i ) total += n.z.baseDistances[i]; }
			if( total < distance )
			{
				distance = total;
				best = n.z.p;
			}
		}
		return best;
	}
	
	public Point getRandomUntakenAdjacentPoint( float x, float y )
	{
		Zone z = la.getZone( x, y );
		List<Neighbor> ns = z.neighbors.stream().filter( n -> n.z.p.team == -1 ).collect( Collectors.toList() );
		return ns.get( MathUtils.random( ns.size()-1 ) ).z.p;
	}
	
	//This is the main game phase
	public void battlePhase( Player me, GameState state )
	{
		while( me.money >= Unit.unitCost )
		{
			send( ClientMessages.BUILDUNIT, null );
			me.money -= Unit.unitCost;
		}
		
		//Update Armies
		for( int i = 0; i < armies.size(); i++ )
		{
			Army army = armies.get( i );
			army.update();
			if( army.units.size() == 0 )
			{
				armies.remove( i );
				i--;
			}
		}
		
		//If unit isn't a part of an army, either add it to a nearby army, or create a new one
		for( UnitWrapper uw : state.units )
		{
			Unit u = uw.getUnit();
			if( u.owner != me.playerId ) continue;
			
			if( !unitArmyMap.containsKey( u ) )
			{
				for( Army army : armies )
				{
					if( army.location.dst( u.pos ) < state.map.tileWidth*5 )
					{
						army.units.add( u );
						unitArmyMap.put( u, army );
						break;
					}
				}
				//If we still didnt find an army to add to, start a new one
				if( !unitArmyMap.containsKey( u ) )
				{
					Army army = new Army();
					army.location = new Vector2( u.pos.x, u.pos.y );
					army.units.add( u );
					unitArmyMap.put( u, army );
					armies.add( army );
				}
			}
		}
		
		
		//TODO: figure out line of sight calculations (they are critical to gameplay)
		//For each army
		armyLoop:
		for( int i = 0; i < armies.size(); i++ )
		{
			Army army = armies.get( i );
			//TODO: possible add logic to change destination if things change along the way
			//Don't do army logic while moving
			if( army.moving ) continue;
			
			//For each zone on border, give a score based on the relative strength vs the closest enemy zone
			Zone bestBorderZone = this.findBestReinforceZone( army );
			if( bestBorderZone == null ) continue; //TODO: under what circumstances is this null
			
			//If at home base and the home base is not the frontline
			if( homeBase.isHere( army.location, state ) && isBorder( la.getZone( homeBase.pos.x, homeBase.pos.y ) ) )
			{
				//If more than n units
				if( army.units.size() > 2 )
				{
					//Send army to the "Best Border Zone"
					if( bestBorderZone != null )
					{
						army.moveGrid( bestBorderZone.p.randomAdjacent( state.map ) );
						continue armyLoop;
					}
				}
			}
			else //If not at home base
			{
				Zone currentZone = la.getZone( army.location.x, army.location.y );
				
				//If taking a point, never give up!
				if( currentZone.p.team != me.team && currentZone.p.isCapturable( state ) )
				{
					continue;
				}
				
				//If close to another army Merge armies
				for( int j = 0; j < armies.size(); j++ )
				{
					Army bArmy = armies.get( j );
					if( bArmy == army || bArmy.moving ) continue;
					
					if( army.location.dst( bArmy.location ) < state.map.tileWidth * 3 )
					{
						//Add all units to this army
						for( Unit u : bArmy.units )
						{
							army.units.add( u );
							unitArmyMap.put( u, army );
						}
						
						//Remove army
						armies.remove( j );
						j--;
					}
				}
				
				//Determine if on a border zone
				boolean borderZone = false;
				for( Neighbor n : currentZone.neighbors )
				{
					if( n.z.p.team != me.team )
					{
						borderZone = true;
						break;
					}
				}
				
				//If not on a border zone
				//Move to a good border zone
				
				if( !borderZone )
				{
					Zone best = findBestReinforceZone( army );
					//Send army to the weakest point
					if( best != null )
					{
						army.moveGrid( best.p.randomAdjacent( state.map ) );
						continue armyLoop;
					}
				}
				
				//Look at closest enemy zone
				//Decide if you can attack, then attack if so
				Zone attackZone = this.findBestAttackZone( army );
				if( attackZone != null )
				{
					army.moveGrid( attackZone.p.randomAdjacent( state.map ) );
					continue armyLoop;
				}
				
				//Finally, if there's nothing to do, check and see if we dwarf the enemy in size
				//If we do, choose an enemy point and attack it
				//This is because sometimes there are "natural" expansions behind the frontline, and the frontline 
				//is the enemy base, which we can't take. normally if this happens we are overpowering them anyway
				//so this check helps us not gridlock
				int ourSize = 0, theirSize = 0;
				for( UnitWrapper uw : state.units )
				{
					Unit u = uw.getUnit();
					if( state.playerMap.get( u.owner ).team == me.team )
					{
						ourSize++;
					}
					else
					{
						theirSize++;
					}
				}
				
				if( ourSize > theirSize * 2.5f || army.units.size() > 100 )
				{
					//ci.sl.received( fc, new Message( MessageType.MESSAGE, "Full Attack" ) );
					for( Point b: state.map.points )
					{
						if( b.team != me.team && b.isCapturable( state ) )
						{
							GridPoint2 adj = b.randomAdjacent( state.map );
							army.moveGrid( adj );
						}
					}
				}
			}
		}
				
	}
	
	public boolean isBorder( Zone z )
	{
		for( Neighbor n : z.neighbors )
		{
			if( n.z.p.team != c.me.team )
			{
				return true;
			}
		}
		
		return false;
	}
	
	public float reinforceScoreZone( Zone z, Army army )
	{
		if( z.p.team != c.me.team )
		{
			return -1;
		}
		
		float zScore = 0;
		
		boolean sameZone = la.getZone( army.location.x, army.location.y ) == z;
		
		if( sameZone )
		{
			zScore += 30;
		}
		
		int numUnits = numUnitsInZone( z, la );
		
		boolean isBorder = isBorder( z );
		
		for( Neighbor n : z.neighbors )
		{
			if( n.z.p.team != c.me.team )
			{
				//We want to reinforce zones that neighbor zones with lots of enemies
				zScore += numUnitsInZone( n.z, la );
			}
			else
			{
				// a zone that borders more of our own zones should get a higher score
				zScore += 5;
			}
		}
		
		if( isBorder )
		{
			//We don't want to visit a zone with more units visible than the size of our army
			int visible = 0;
			for( Zone v : z.visible )
			{
				visible += numUnitsInZone( v, la, u -> u.team != c.me.team ); 
			}
			//lack of visible units shouldn't be a bonus
			zScore += Math.max( (army.units.size() + numUnits) - visible, 0 );
			
			//If border zone is empty, give a big bonus
			if( numUnits == 0 )
			{
				zScore += 50;
			}
			
			if( numUnits * 10 < army.units.size() )
			{
				zScore += 100;
			}
			
			zScore -= z.baseDistances[c.me.team];
		}
		else //if not the border
		{
			//We should reinforce the border first
			zScore -= 100;
		}
		
		//We want to also veer towards not reinforcing points with lots of units
		zScore -= numUnits * 3;
		
		return zScore;
	}
	
	public Zone findBestReinforceZone( Army army )
	{
		Zone best = null;
		float score = -1000;
		for( Zone z : la.zones )
		{
			if( z.p.team != c.me.team ) continue;
			
			float zScore = reinforceScoreZone( z, army );
						
			if( zScore > score )
			{
				score = zScore;
				best = z;
			}
		}
		return best;
	}
	
	public float attackScoreZone( Zone z, Army army )
	{
		int zoneStrength = numUnitsInZone( z, la );
		
		//If the zone isn't capturable, but we have 10x the units on it, attack it anyway
		if( !z.p.isCapturable( c.state ) && zoneStrength > army.units.size() * .1f ) return 1;
		
		//If army size * .75 is greater than the number of units on zone, this will be > 1
		//This means we will always attack weakest neighbor (TODO: correct?)
		float score = (army.units.size() * .75f) / zoneStrength; 
		
		if( z.neighbors.size() <= 2 ) 
		{
			score += .1f;
		}
		
		return score;
	}
	
	public Zone findBestAttackZone( Army army )
	{
		Zone currentZone = la.getZone( army.location.x, army.location.y );
		
		Zone best = null;
		//Don't accept zones with score < 1
		float score = .99999999f;
		for( Neighbor n : currentZone.neighbors )
		{
			Zone z = n.z;
			if( z.p.team == c.me.team ) continue;
			
			float zScore = attackScoreZone( z, army );
						
			if( zScore > score )
			{
				score = zScore;
				best = z;
			}
		}
		return best;
	}
	
	// Represents a group of units that should move together
	public class Army
	{
		Vector2 location = new Vector2();
		ArrayList<Unit> units = new ArrayList<>();
		boolean moving = false;
		
		public void update()
		{
			for( int i = 0; i < units.size(); i++ )
			{
				if( units.get( i ).remove )
				{
					unitArmyMap.remove( units.remove( i ) );
					i--;
				}
			}
			
			if( moving )
			{
				boolean canStop = true;
				for( Unit u : units )
				{
					if( u.isMoving() )
					{
						canStop = false;
						break;
					}
				}
				if( canStop )
				{
					moving = false;
				}
			}
		}
		
		public void moveGrid( GridPoint2 p )
		{
			moveGrid( p.x, p.y );
		}

		public void moveGrid( int x, int y )
		{
			move( (x+.5f)*c.state.map.tileWidth, (y+.5f)*c.state.map.tileHeight );
		}
		
		public void move( float x, float y )
		{
			location.x = x;
			location.y = y;
			ArrayList<Integer> unitIdsToMove = new ArrayList<Integer>();
			for( Unit u : units )
			{
				unitIdsToMove.add( u.syncId );
			}
			send( ClientMessages.MOVEUNITS, new Packets.MoveUnitPacket( location, unitIdsToMove ) );
			moving = true;
		}
	}
}
