package com.danwink.strategymass.nethelpers;

public enum ServerMessages
{
	JOINSUCCESS, JOINFAIL, //Client JOIN confirmation
	MAP, //Map Data
	BUILDUNITSUCCESS, BUILDUNITFAIL, //Client BUILDUNIT confirmation
	BUILDUNIT, //Sent on new unit creation
	UNITUPDATE, //Unit movement update
	BULLET, //On bullet shot
}
