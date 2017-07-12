package com.danwink.fieldaccess;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FieldManager
{
	Class target;
	
	public FieldManager( Class target )
	{
		this.target = target;
	}
	
	public List<Field> getFields()
	{
		return Arrays.asList( target.getDeclaredFields() )
			.stream()
			.filter( f -> {
				return f.isAnnotationPresent( Accessable.class ) && Modifier.isStatic( f.getModifiers() );
			})
			.collect( Collectors.toList() );
	}
}
