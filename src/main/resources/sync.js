{
	"$settings":{
		
	},
	"$parts":{
		"testport1":{"$channel":0, "$port":"port1"},
		"testport2":{"$channel":0, "$port":"port2"}
	},
	"$patterns":{
		"test1":{
			"$body":[
					    {"$notes":"C", "$length":"2", "$pan":0 },
					    {"$notes":"C", "$length":"2", "$pan":0 }
			],
			"$parameters":{
				"$length":"8"
			}
		},
		"test2":{
			"$body":[
			         {"$notes":"C", "$length":"2", "$pan":127 },
			         {"$notes":"C", "$length":"2", "$pan":127 }
			 ],
			"$parameters":{
				"$length":"8"
			}
	}
	},
	"$sequence":[
	    {
	    	"$beats":"8/8",
	    	"$patterns":{
	    		"testport1":["test1"],
	    		"testport2":["test2"]
	    	}
	    },
	    {
	    	"$beats":"8/8",
	    	"$patterns":{
	    		"testport1":["test1"],
	    		"testport2":["test2"]
	    	}
	    }
	]
}