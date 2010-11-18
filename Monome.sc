//-----------------------------------------------------------------
//
// Clean interactions with the Monome
//
//  Origanally written by: 
//  __ Daniel Jones
//     http://www.erase.net
//  
//  Heavily modified by:
//  __ Tristan Strange
//     tristan.strange@gmail.com
//
// Run serialio with:
//    ./serialio localhost:57120
//
// Usage:
//    m = Monome.new(host, port, prefix, height, width);
//    m = Monome.new;
//    m.action = { |x, y, on|
//      [x, y, on].postln;
//    };
//    m.led(5, 6, 1);
//    m.led_row(4, 255);
//    m.intensity(0.5);
//    m.clear;
//
//-----------------------------------------------------------------

Monome {
	
	var <host, <port;
	var <prefix;

	var <height, <width;

	var incomingPressResponder, monomeAddress;

	var <>action;

	*new { | host = "127.0.0.1", port = 8080, prefix = "/box", height = 16, width = 16 |
		^super.newCopyArgs(host, port, prefix, height, width).init;
	}
	
	*emu { | port = 57120 |
		// spawn emulator
		MonomEm.new(port: port);
		^this.new(port: port).init;
	}
	
	init {
		// set up OSC
		this.initMonomePressesResponder;
		monomeAddress = NetAddr(host, port);
	}

	initMonomePressesResponder {
		incomingPressResponder = OSCresponderNode(nil, prefix ++ "/press", { |time, resp, msg|
			if (action.notNil)
			   { action.value(msg[1], msg[2], msg[3]); };
		});
		incomingPressResponder.add;
	}

	prefix_ { | newPrefix |
		newPrefix.notNil.if {
			// prefix a / to the new prefix if it doesn't have one
			(newPrefix.asString.at(0) != '/').if { newPrefix = "/" ++ newPrefix };
		
			// update the prefix
			prefix = newPrefix;
			monomeAddress.sendMsg("/sys/prefix", prefix);
		
			// reinitialise OSC responder
			incomingPressResponder.remove;
			this.initMonomePressesResponder;
		};
	}

	led { | x, y, on = 1 |
		monomeAddress.sendMsg(prefix ++ "/led", x.asInteger, y.asInteger, on.asInteger);
	}

	led_row { | y, on = 255, on2 |
		(on2.isNil).if({
			monomeAddress.sendMsg(prefix ++ "/led_row", y.asInteger, on.asInteger);
		}, {
			monomeAddress.sendMsg(prefix ++ "/led_row", y.asInteger, on.asInteger, on2.asInteger);
		});
	}

	led_col { | x, on = 255, on2 |
		(on2.isNil).if({ 
			monomeAddress.sendMsg(prefix ++ "/led_col", x.asInteger, on.asInteger);
		}, {
			monomeAddress.sendMsg(prefix ++ "/led_col", x.asInteger, on.asInteger, on2.asInteger); 
		});	
	}
	
	intensity { | i |
		monomeAddress.sendMsg(prefix ++ "/intensity", i);
	}

	clear { | on = 0 |
		width.do { |i| this.led_col(i, on * 255, on * 255) };
	}
	
	test { | on = 1 |
		monomeAddress.sendMsg(prefix ++ "/test", on.asInteger);
	}
}
