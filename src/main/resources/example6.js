{
  "$include":[
    "example6-sequences.js"
  ],
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
      "$length":"8"
    }
  }
}
