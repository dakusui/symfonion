{
	"$parts":{
		"vocal":{ "$channel":0, "$port":"port1" },
		"piano":{ "$channel":1, "$port":"port1"  },
		"guitar":{ "$channel":2, "$port":"port1"  },
		"base":{ "$channel":3, "$port":"port2"  },
		"drums":{ "$channel":9, "$port":"port2"  }
	},
	"$notemaps":{
	},
	"$patterns":{
		"melody1":{
		    "$notemap":"$normal",
		    "$body":[
		        "F#>C#>A#",
		        "E>BG#",
		        {},
		        {"$length":"8"},
		        {
		        	"$notes":"G#>D#>B",    
		        	"$length":"8"
		        }
		    ],
		    "$parameters":{
		        "$velocitybase":106,
		        "$velocitydelta":10,
		        "$gate":0.8,
		        "$length":"4",
		        "$transpose":0
		    }
		},
		"melody2":{
			"$body":[
				"B>>>","B>>>","B>>>","B>>>","B>>>","B>>>","B>>>","B>>>","B>>>","B>>>","B>>>","B>>>","B>>>","B>>>","B>>>","B>>>",
			],
			"$parameters":{
				"$length":"16"
			}
		},
		"setting":{
			"$body":[
			     {"$tempo":150, "$reverb":127, "$chorus":127},
			]
		},
		"effect1":{
		    "$notemap":"$normal",    // "$melody", "$pattern", and "$sequence"
		    "$body":[
		        {"$pitch":[64,,,,,,0]},
		    ]
		},
		"effect2":{
			"$body":[
				{ "$volume":[106,,,,,,,,127] }
			],
			"$parameters":{
				"$length":"1"
			}
		},
		"base-setting":{
			"$body":[
			    {"$program":13, "$bank":83.2 }
		    ]
		},
		"base1":{
			"$body":[
					{  "$notes":"B<<" },
					{  "$notes":"B<", "$length":"16"}, {  "$notes":"B<", "$length":"16"},
					{  "$notes":"B<<" },
					{  "$notes":"B<", "$length":16}, {  "$notes":"B<", "$length":"16"},
					{  "$notes":"B<<", "$chorus":10},
					{  "$notes":"B<" },
					{  "$notes":"B<<" },
					{  "$notes":"B<", "$length":16}, {  "$notes":"B<", "$length":"16"},
			],
			"$parameters":{
				"$length":"8",
		        "$velocitybase":106
			}
		},
		"rhythm1":{
			"$notemap":"$percussion",
			"$body":[
				"B++HC", "H-", "O", "H--",
				"BS+H",  "H-", "O", "",
				"B+H",   "H-", "O", "H--",
				"BS+H",  "H-", "O", ""
			],
			"$parameters":{
		        "$velocitybase":106,
		        "$velocitydelta":10,
		        "$gate":0.8,
		        "$length":"16",
		        "$transpose":0 // does not make sense for rhythm patterns
			},
			"$percussionkit":null
		}
	},
	"$grooves":{
		"16beats":{
			"$beatlength":"1/16",
			"$beats":[
				26, 22, 25, 23, 26, 22, 25, 23, 26, 22, 25, 23, 26, 22, 25, 23 
			]
		}
	},
	"$sequence":[
  	    {
			"$beats":"1/4",
			"$patterns":{
				"vocal":["setting"],
				"piano":["setting"],
				"guitar":["setting"],
				"base":["base-setting"]
			},
			"$groove":"16beats"
		},
	    {
			"$beats":"16/16",
			"$patterns":{
				"vocal":[],
				"piano":[],
				"guitar":["melody1"],
				"base":["base1"],
				"drums":["rhythm1"]
			},
			"$groove":"16beats"
		},
	    {
			"$beats":"16/16",
			"$patterns":{
				"vocal":["melody1"],
				"piano":["melody1"],
				"guitar":["melody1", "melody2"],
				"base":["base1"],
				"drums":["rhythm1"]
			},
			"$groove":"16beats"
		},
	    {
			"$beats":"16/16",
			"$patterns":{
				"vocal":["melody1", "effect1"],
				"piano":["melody1", "effect1"],
				"base":["base1", "effect1"],
				"drums":["rhythm1"]
			},
			"$groove":"16beats"
		},
	    {
			"$beats":"16/16",
			"$patterns":{
				"vocal":["melody1", "effect1"],
				"piano":["melody1", "effect1"],
				"base":["base1", "effect1"],
				"drums":["rhythm1"]
			},
			"$groove":"16beats"
		}
	]
}