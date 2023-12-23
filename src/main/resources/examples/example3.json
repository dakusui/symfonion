{
	"$settings":{
		"$mididevice":"jv"
	},
	"$parts":{
		"test":{"$channel":0, "$port":"jv1010"},
		"test2":{"$channel":9, "$port":"jv1010"}
	},
	"$patterns":{
		"sysextest1":{
			"$body":[
			         { "$sysex":["$set-syscommon-soundmode-perf"] },
			         { "$sysex":["$jv-set-currentperf", 12] },
			         { "$sysex":["$jv-init-part",  0,  0]},
			         { "$sysex":["$jv-init-part",  1,  1]},
			         { "$sysex":["$jv-init-part",  2,  2]},
			         { "$sysex":["$jv-init-part",  3,  3]},
			         { "$sysex":["$jv-init-part",  4,  4]},
			         { "$sysex":["$jv-init-part",  5,  5]},
			         { "$sysex":["$jv-init-part",  6,  6]},
			         { "$sysex":["$jv-init-part",  7,  7]},
			         { "$sysex":["$jv-init-part",  8,  8]},
			         { "$sysex":["$jv-init-part",  9,  9]},
			         { "$sysex":["$jv-init-part", 10, 10]},
			         { "$sysex":["$jv-init-part", 11, 11]},
			         { "$sysex":["$jv-init-part", 12, 12]},
			         { "$sysex":["$jv-init-part", 13, 13]},
			         { "$sysex":["$jv-init-part", 14, 14]},
			         { "$sysex":["$jv-init-part", 15, 15]},
			         { "$sysex":["$jv-set-patchnum-pr-a", "$channel", 122] },
			         { "$sysex":["$jv-set-mixefx-send-level", "$channel", 127] },
			         { "$sysex":["$jv-set-output-level", "$channel", 127] },
			         { "$sysex":["$jv-set-output-assign", "$channel", "$output-EFX"] },
			         { "$sysex":["$set-perfcommon-efx-distortion", "$currentperf", 127, 0, 3, 30, 30, 32] },
			         { "$sysex":["$set-perfcommon-efx", "$currentperf", "$output-MIX", 22, 0, 30, "$contsource-OFF", 63, "$contsource-OFF", 0 ] },
			         { "$sysex":["$set-perfcommon-chorus", "$currentperf", 0, 106, 106, 16,  64, 2] },
			         { "$sysex":["$set-perfcommon-reverb", "$currentperf", "$reverb-type-STAGE1", 106, 127, "$reverb-hfdump-2000", 0] },
			         { "$sysex":["$set-perfcommon-tempo", "$currentperf", 100] },
			         { "$sysex":["$jv-refresh-currentperf"] }
			],
			"$length":0
		},
		"sysextest2":{
			"$body":[
			         { "$sysex":["$set-syscommon-soundmode-perf"] },
			         { "$sysex":["$jv-set-currentperf", 12] },
			         { "$sysex":["$jv-set-patchnum-pr-b", "$channel",  1] },
			         { "$sysex":["$jv-set-efx-level", "$channel",  64] },
			         { "$sysex":["$jv-set-output-level", "$channel", 100] },
			         { "$sysex":["$jv-set-output-assign", "$channel", "$output-EFX"] },
			         { "$sysex":["$set-perfcommon-efx-source", "$currentperf", "$channel"] },
			         { "$sysex":["$set-perfcommon-chorus", "$currentperf", 0, 106, 106, 16,  64, 2] },
			         { "$sysex":["$set-perfcommon-reverb", "$currentperf", "$reverb-type-STAGE1", 106, 127, "$reverb-hfdump-2000", 0] },
			         { "$sysex":["$set-perfcommon-tempo", "$currentperf", 32] },
			         { "$sysex":["$jv-refresh-currentperf"] }
			],
			"$length":0
		},
		"sysextest3":{
			"$body":[
			    { "$sysex":["$jv-set-patchnum-xp-techno", 0] }
			],
			"$length":0
		},
		"test1":{
			"$body":[
			         {"$sysex":["$set-perfcommon-efx-param", "$currentperf", 2, 127], "$length":0 },
			         {"$sysex":["$jv-refresh-currentperf"], "$length":0 },
			         {"$notes":"C", "$length":"2", "$pan":127, "$chorus":32 },
			         {"$sysex":["$set-perfcommon-efx-param", "$currentperf", 2, 0], "$length":0 },
			         {"$sysex":["$jv-refresh-currentperf"], "$length":0 },
			         {"$notes":"C", "$length":"2", "$pan":0 }
			],
			"$parameters":{
				"$length":"8"
			}
		}
	},
	"$sequence":[
        {
        	"$beats":"1/8",
        	"$patterns":{
	    		"test":["sysextest1"]
	    	}
	    },
        {
	    	"$beats":"8/8",
	    	"$patterns":{
	    		"test":["test1"]
	    	}
	    }
	]
}
