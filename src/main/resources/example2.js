{
	"$settings":{
		
	},
	"$parts":{
		"test":{"$channel":0}
	},
	"$patterns":{
		"test1":{
			"$body":[
					    {"$notes":"C", "$length":"2", "$pan":127, "$program":2},
					    {"$notes":"C", "$length":"2", "$pan":0}
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
	    		"test":["test1"]
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