package com.danwink.strategymass.ai;

import com.danwink.strategymass.ai.MapAnalysis.Neighbor;
import com.danwink.strategymass.ai.MapAnalysis.Zone;
import com.danwink.strategymass.game.GameClient;
import com.danwink.strategymass.game.objects.Unit;
import com.danwink.strategymass.game.objects.UnitWrapper;

public class AIAPI
{
	GameClient c;
	
	public AIAPI( GameClient c )
	{
		this.c = c;
	}
	
	public int numUnitsInZone( Zone z, MapAnalysis ma )
	{
		int count = 0;
		for( UnitWrapper uw : c.state.units )
		{
			Unit u = uw.getUnit();
			Zone uZone = ma.getZone( u.pos.x, u.pos.y ); 
			if( uZone != null && uZone.p.id == z.p.id )
			{
				count++;
			}
		}
		return count;
	}
	
	public int numUnitsInZone( Zone z, MapAnalysis ma, Filter<Unit> filter )
	{
		int count = 0;
		for( UnitWrapper uw : c.state.units )
		{
			Unit u = uw.getUnit();
			Zone uZone = ma.getZone( u.pos.x, u.pos.y ); 
			if( uZone != null && uZone.p.id == z.p.id && filter.valid( u ) )
			{
				count++;
			}
		}
		return count;
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
	
	public int getVisibleEnemies( Zone z, MapAnalysis ma )
	{
		return z.visible.stream()
			.mapToInt( v -> numUnitsInZone( v, ma, u -> u.team != c.me.team ) )
			.sum();
	}
	
	public interface Filter<E>
	{
		public boolean valid( E e );
	}
}
