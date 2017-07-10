package com.danwink.strategymass.ai;

import java.util.ArrayList;
import java.util.LinkedList;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Vector2;
import com.danwink.strategymass.ai.MapAnalysis.Zone;
import com.danwink.strategymass.game.GameClient;
import com.danwink.strategymass.game.GameState;
import com.danwink.strategymass.game.objects.Player;
import com.danwink.strategymass.game.objects.Unit;
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
	public float zoneIsBorder = 50;
	public float neighborUnitsScalar = 1;
	public float neighborCountScalar = 6;
	public float distanceFromHomeBaseScalar = 20;
	
	MapAnalysis ma;
	AIAPI api;
	
	StateMachine<GameClient, AIState> sm;
	
	LinkedList<BattleGroup> groups = new LinkedList<>();
	
	ArrayList<ZoneScore> globalScores = new ArrayList<>();

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
			sm = new DefaultStateMachine<>( c, AIState.EXPAND );
			
			//Initialize globalScores store
			ma.zones.forEach( z -> globalScores.add( new ZoneScore( z ) ) );
		}
		
		//Build units if we have enough money
		
		sm.update();
	}
	
	public enum AIState implements State<GameClient>
	{
		EXPAND() {
			public void update( GameClient c )
			{
				//If all points are taken, move to main state
				
				//For each unit
					//If not moving
						//Find best point to take		
			}
		},
		MAIN() {
			public void update( GameClient c )
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
		
		public void enter( GameClient c ) {}
		
		public void exit( GameClient c ) {}

		public boolean onMessage( GameClient c, Telegram m )
		{
			return false;
		}
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
			
			// + How many of our own units are there;
			score += api.numUnitsInZone( z, ma, u -> u.team == c.me.team ) * ownUnitsInZoneScalar;
			// + If the zone is on the border
			if( isBorder ) score += zoneIsBorder;
			// + Neighboring zones have enemy units
			score += neighborUnits * neighborUnitsScalar;
			// - Number of neighbors
			score -= z.neighbors.size() * neighborCountScalar;
			// - Distance from home base
			
			s.score = score;
		});
	}
	
	public class BattleGroup
	{
		LinkedList<Unit> units = new LinkedList<>();
		Vector2 location = new Vector2();
		
		public void update( float dt )
		{
			//Create group specific zone rankings
			//Add global and group zone rankings
			//If highest ranked point != current point
				//Move to highest ranked point
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
