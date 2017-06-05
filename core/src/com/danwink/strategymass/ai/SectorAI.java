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

public class SectorAI extends Bot
{	
	MapAnalysis la;
	boolean battlePhase = false;
	ArrayList<Army> armies = new ArrayList<>();
	HashMap<Unit, Army> unitArmyMap = new HashMap<>();
	Point homeBase;
	
	public void update( Player me, GameState state )
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
		if( !battlePhase ) expandPhase( me, state );
		else battlePhase( me, state );
		
	}
	
	public void reset()
	{
		la = null;
		battlePhase = false;
		armies.clear();
		unitArmyMap.clear();
		homeBase = null;
	}
	
	//In this phase we try to expand and take every untaken point
	public void expandPhase( Player me, GameState state )
	{
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
				Point b = getBestUntakenAdjacentPoint( u.pos.x, u.pos.y, me.team ); 
						
				if( b == null )
				{
					b = findPointShortestPath( u.pos.x, u.pos.y, la.graph, tb -> { return tb.team == -1; });
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
	
	public Point getBestUntakenAdjacentPoint( float x, float y, int team )
	{
		Zone z = la.getZone( x, y );
		Point best = null;
		int distance = 100000;
		for( Neighbor n : z.neighbors )
		{
			if( n.z.p.team != -1 ) continue;
			
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
		armyBreak:
		for( int i = 0; i < armies.size(); i++ )
		{
			Army army = armies.get( i );
			//TODO: possible add logic to change destination if things change along the way
			//Don't do army logic while moving
			if( army.moving ) continue;
			
			Zone bestBorderZone = findBestBorderZone();
			
			//If at home base and the home base is not the frontline
			if( homeBase.isHere( army.location, state ) && bestBorderZone.p != homeBase )
			{
				//If more than n units (5?)
				if( army.units.size() > 2 )
				{
					//For each zone on border, give a score based on the relative strength vs the closest enemy zone
					
					//Send army to the weakest point
					if( bestBorderZone != null )
					{
						army.moveGrid( bestBorderZone.p.randomAdjacent( state.map ) );
						break armyBreak;
					}
				}
			}
			else //If not at home base
			{
				Zone currentZone = la.getZone( army.location.x, army.location.y );
				
				//If taking a point, never give up!
				if( currentZone.p.team != me.team )
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
				
				//If not on a border zone
				//Move to a good border zone
				boolean borderZone = false;
				for( Neighbor n : currentZone.neighbors )
				{
					if( n.z.p.team != me.team )
					{
						borderZone = true;
						break;
					}
				}
				if( !borderZone )
				{
					Zone best = findBestBorderZone();
					//Send army to the weakest point
					if( best != null )
					{
						army.moveGrid( best.p.randomAdjacent( state.map ) );
						break armyBreak;
					}
				}
				
				//Look at closest enemy zone
				//Decide if you can attack, then attack if so
				for( Neighbor n : currentZone.neighbors )
				{
					if( n.z.p.team == me.team ) continue;
					
					if( !n.z.p.isCapturable( state ) ) continue;
					
					int nZoneStrength = numUnitsAtPoint( n.z.p );
					
					if( nZoneStrength < army.units.size() * .75f )
					{
						army.moveGrid( n.z.p.randomAdjacent( state.map ) );
						break armyBreak;
					}
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
				
				if( ourSize > theirSize * 2.5f )
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
	
	public Zone findBestBorderZone()
	{
		Zone best = null;
		int score = -1000;
		for( Zone z : la.zones )
		{
			if( z.p.team != c.me.team ) continue;
			
			int zScore = 5; //Each point gets a base amount so even if its unoccupied its worth creating a front against
			boolean isBorder = false;
			for( Neighbor n : z.neighbors )
			{
				if( n.z.p.team != c.me.team )
				{
					zScore += numUnitsAtPoint( n.z.p );
					isBorder = true;
				}
				else
				{
					zScore += 5; // a zone that borders more of our own zones should get a higher score
				}
			}
			
			zScore -= numUnitsAtPoint( z.p );
			
			if( !isBorder )
			{
				zScore -= 100;
			}
			
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
