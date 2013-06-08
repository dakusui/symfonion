{
	"$parts":{
		"vocal":{ "$channel":0, "$port":"port1" }
	},
	"$notemaps":{
	},
	"$patterns":{
		"melody1":{
		    "$notemap":"$normal",
		    "$body":[
		             "C", "D", "E", "F", "C", "D", "E","F",
		             "C", "D", "E", "F", "C", "D", "E","F"
		    ],
		    "$parameters":{
		        "$velocitybase":106,
		        "$velocitydelta":10,
		        "$gate":0.8,
		        "$length":"16",
		        "$transpose":0
		    }
		},
		"setting":{
			"$body":[
			     {"$tempo":120, "$reverb":127, "$chorus":127},
			]
		}
	},
	"$grooves":{
		"16beats":[
		    { "$length":"16", "$ticks":28, "$accent":30 },
		    { "$length":"1/6", "$ticks":20, "$accent":-10 },
		    { "$length":"16", "$ticks":26, "$accent":10 },
		    { "$length":"16", "$ticks":22, "$accent":-5},
		    { "$length":"16", "$ticks":28, "$accent":20 },
		    { "$length":"16", "$ticks":20, "$accent":-8 },
		    { "$length":"16", "$ticks":26, "$accent":10 },
		    { "$length":"16", "$ticks":22, "$accent":-4 },
		    { "$length":"16", "$ticks":28, "$accent":25 },
		    { "$length":"16", "$ticks":20, "$accent":-8 },
		    { "$length":"16", "$ticks":26, "$accent":10 },
		    { "$length":"16", "$ticks":22, "$accent":-5 },
		    { "$length":"16", "$ticks":28, "$accent":15 },
		    { "$length":"16", "$ticks":20, "$accent":-8 },
		    { "$length":"16", "$ticks":26, "$accent":10 },
		    { "$length":"16", "$ticks":22, "$accent":-10 }
		]
	},
	"$sequence":[
	    {
			"$beats":"16/16",
			"$patterns":{
				"vocal":["melody1"]
			},
			"$groove":"16beats"
		},
	    {
			"$beats":"16/16",
			"$patterns":{
				"vocal":["melody1"]
			},
			"$groove":"16beats"
		}
	]
}