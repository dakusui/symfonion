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
	        "$velocitybase":106,
	        "$velocitydelta":10,
	        "$gate":0.8,
	        "$length":"16",
	        "$transpose":0
		},
		"setting":{
			"$body":[
			     {"$tempo":120, "$reverb":127, "$chorus":127},
			]
		}
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