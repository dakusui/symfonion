{
	"deviceid":["$hex", "10"],
	"currentperf":0,
	"reverb-type-ROOM1":0,
	"reverb-type-ROOM2":1,
	"reverb-type-STAGE1":2,
	"reverb-type-STAGE2":3,
	"reverb-type-HALL1":4,
	"reverb-type-HALL2":5,
	"reverb-type-DELAY":6,
	"reverb-type-PAN-DLY":7,
	"reverb-hfdump-200":0,
	"reverb-hfdump-250":1,
	"reverb-hfdump-315":2,
	"reverb-hfdump-400":3,
	"reverb-hfdump-500":4,
	"reverb-hfdump-630":5,
	"reverb-hfdump-800":6,
	"reverb-hfdump-1000":7,
	"reverb-hfdump-1250":8,
	"reverb-hfdump-1600":9,
	"reverb-hfdump-2000":10,
	"reverb-hfdump-2500":11,
	"reverb-hfdump-3150":12,
	"reverb-hfdump-4000":13,
	"reverb-hfdump-5000":14,
	"reverb-hfdump-6300":15,
	"reverb-hfdump-8000":16,
	"reverb-hfdump-BYPASS":17,
	"output-MIX":"MIX",
	"output-EFX":"EFX",
	"output-DIRECT1":"DIRECT1",
	"output-DIRECT2":"DIRECT2",
	"output-PATCH":"PATCH",
	"contsource-OFF":0,
	"contsource-SYSCTRL1":1,
	"contsource-SYSCTRL2":2,
	"contsource-MODULATION":3,
	"contsource-BREATH":4,
	"contsource-FOOT":5,
	"contsource-VOLUME":6,
	"contsource-PAN":7,
	"contsource-EXPRESSION":8,
	"contsource-PITCHBEND":9,
	"contsource-AFTERTOUCH":10,
	"exp_patch":["$lambda", ["$patchnum"],
	             ["$cons", ["$shift", -4, ["$&", ["$hex", "f0"], "$patchnum"] ],
	              ["$cons", ["$&", ["$hex", "0f"], "$patchnum"],
	               "$nil"
	               ]
	             ]
	],
	"patch":["$lambda", ["$patchgroup", "$patchnum"],
	         ["$cond",
	          [ ["$eq", "$patchgroup", "USER"], 
	            ["$append", ["$quote", [0, 1] ], ["$exp_patch", "$patchnum"] ] 
	          ],
	          [ ["$eq", "$patchgroup", "CARD"], 
	            ["$append", ["$quote", [0, 2] ], ["$exp_patch", "$patchnum"] ] 
	          ],
	          [ ["$eq", "$patchgroup", "PR-A"], 
	            ["$append", ["$quote", [0, 3] ], ["$exp_patch", "$patchnum"] ] 
	          ],
	          [ ["$eq", "$patchgroup", "PR-B"], 
	            ["$append", ["$quote", [0, 4] ], ["$exp_patch", "$patchnum"] ] 
	          ],
	          [ ["$eq", "$patchgroup", "PR-C"], 
	            ["$append", ["$quote", [0, 5] ], ["$exp_patch", "$patchnum"] ] 
	          ],
	          [ ["$eq", "$patchgroup", "PR-D"], 
	            ["$append", ["$quote", [0, 6] ], ["$exp_patch", "$patchnum"] ] 
	          ],
	          [ ["$eq", "$patchgroup", "PCM" ], 
	            ["$append", ["$quote", [1, 7] ], ["$exp_patch", "$patchnum"] ] 
	          ],
	          [ ["$eq", "$patchgroup", "XP-A"], 
	            ["$append", ["$quote", [2, 1] ], ["$exp_patch", "$patchnum"] ] 
	          ],
	          [ ["$eq", "$patchgroup", "XP-B"], 
	            ["$append", ["$quote", [2, 2] ], ["$exp_patch", "$patchnum"] ] 
	          ],
	          [ ["$eq", "$patchgroup", "XP-C"], 
	            ["$append", ["$quote", [2, 3] ], ["$exp_patch", "$patchnum"] ] 
	          ],
	          [ ["$eq", "$patchgroup", "XP-D"], 
	            ["$append", ["$quote", [2, 4] ], ["$exp_patch", "$patchnum"] ] 
	          ],
	          [ ["$eq", "$patchgroup", "XP-E"], 
	            ["$append", ["$quote", [2, 5] ], ["$exp_patch", "$patchnum"] ] 
	          ],
	          [ ["$eq", "$patchgroup", "XP-F"], 
	            ["$append", ["$quote", [2, 6] ], ["$exp_patch", "$patchnum"] ] 
	          ],
	          [ ["$eq", "$patchgroup", "XP-G"], 
	            ["$append", ["$quote", [2, 7] ], ["$exp_patch", "$patchnum"] ] 
	          ],
	          [ ["$eq", "$patchgroup", "XP-H"], 
	            ["$append", ["$quote", [2, 8] ], ["$exp_patch", "$patchnum"] ] 
	          ],
	          [ ["$eq", "$patchgroup", "XP-I"], 
	            ["$append", ["$quote", [2, 9] ], ["$exp_patch", "$patchnum"] ] 
	          ]
	         ]
	],
	"_sumall":["$lambda", ["$values"],
	           ["$let", [["$sum", 0]],
	            ["$dolist", ["$cur", "$values"],
	             ["$setq", "$sum", ["$+", "$sum", "$cur"]],
	             "$sum"
	            ]
	           ]
	],
	"setdata":["$lambda",["$address", "$data"], 
	           ["$let", [["$sum", 0], ["$ret", "$nil"], ["$adr1", 0], ["$adr2", 0], ["$adr3", 0], ["$adr4", 0]],
	            ["$setq", "$ret",
	             ["$cons", ["$hex", "41"],
	              ["$cons", "$deviceid",
	               ["$cons", ["$hex", "6a"],
	                ["$cons", ["$hex", "12"],
	                 ["$cons", ["$eval", ["$setq", "$adr1", ["$shift", -24, ["$&", ["$hex", "ff000000"], "$address"]]]],
	                  ["$cons", ["$eval", ["$setq", "$adr2", ["$shift", -16, ["$&", ["$hex", "00ff0000"], "$address"]]]],
	                   ["$cons", ["$eval", ["$setq", "$adr3", ["$shift",  -8, ["$&", ["$hex", "0000ff00"], "$address"]]]],
	                    ["$cons", ["$eval", ["$setq", "$adr4", ["$shift",   0, ["$&", ["$hex", "000000ff"], "$address"]]]],
	                     "$nil"]
	                   ]
	                  ]
	                 ]
	                ]
	               ]
	              ]
	             ]
	           ],
	           ["$append", "$ret", "$data"],
	           ["$append", "$ret", 
	                ["$cons", 
	                 ["$%", 
	                  ["$-", 128, ["$%", 
	                               ["$+", 
	                                "$adr1", "$adr2","$adr3", "$adr4", ["$_sumall", "$data"]
	                               ], 
	                               128]
	                  ],
	                  128
	                  ],
	                  "$nil"
	                  ]
	           ],
	           "$ret"
	           ]
	],
	"baseaddress-sys":["$lambda", [], 0],
	"baseaddress-syscommon":["$lambda", [], ["$baseaddress-sys"]],
	"set-syscommon-data":["$lambda", ["$offset", "$value"],
	                     ["$setdata", ["$+", "$offset", ["$baseaddress-syscommon"]], "$value"]
	],
	"set-syscommon-soundmode":["$lambda", ["$mode"],
	                           ["$set-syscommon-data",
	                            0, 
	                            ["$cons", "$mode", "$nil"]
	                            ]
	],
	"set-syscommon-soundmode-perf":["$lambda", [],
	                                ["$set-syscommon-soundmode",
	                                 0
	                                 ]
	],
	"set-syscommon-perfnum":["$lambda", ["$perfnum"],
	                         ["$set-syscommon-data",
	                          1,
	                          ["$cons", "$perfnum", "$nil"]
	                         ]
	],
	"set-syscommon-soundmode-patch":["$lambda", [],
	                                 ["$set-syscommon-soundmode",
	                                  1
	                                  ]
	],
	"set-syscommon-patchnum":["$lambda", ["$patchgroup", "$patchnum"],
	                          ["set-syscommon-data", 
	                           ["$hex", "0002"], 
	                           ["$patch", "$patchgroup", "$patchnum"]
	                          ]
	],
	"set-syscommon-soundmode-gm":["$lambda", [],
	                              ["$set-syscommon-soundmode",
	                               2
	                               ]
	],
	"baseaddress-perf":["$lambda", ["$perfnum"],
	                    ["$+", ["$hex", "10000000"], ["$*", "$perfnum", ["$hex", "00010000"]]]
	],
	"baseaddress-perfcommon":["$lambda", ["$perfnum"],
	                          ["$baseaddress-perf", "$perfnum"]
	],
	"set-perfcommon-name":["$lambda", ["$perfnum", "$perfname"] ,
	                       ["$setdata",
	                        ["$baseaddress-perfcommon", "$perfnum"],
	                        ["$split", "$perfname"]
	                       ]
	],
	"set-perfcommon-data":["$lambda", ["$perfnum", "$offset", "$values"] ,
	                       ["$setdata",
	                        ["$+", "$offset", ["$baseaddress-perfcommon", "$perfnum"]],
	                        "$values"
	                        ]
	],
	"set-perfcommon-efx-source":["$lambda", 
	                             ["$perfnum", "$partnum"],
	                             ["$set-perfcommon-data",
	                              "$perfnum",
	                              ["$hex", "000c"],
	                              ["$cons",
	                               ["$cond", 
	                                [
	                                 ["$<", "$partnum", 10], ["$+", "$channel", 1]],
	                                 ["$eval", "$T"], "$channel"
	                               ],
	                               "$nil"
	                              ]
	                             ]
	],
	"set-perfcommon-efx-param":["$lambda",
	                            ["$perfnum", "$paramnum", "$data"],
	                            ["$set-perfcommon-data",
	                             "$perfnum",
	                             ["$+", ["$hex", "000d"], "$paramnum"],
	                             ["$cons", "$data", "$nil"]
	                             ]
	],
	"set-perfcommon-efx-params":["$lambda", 
	                      ["$perfnum",
	                       "$type", 
	                       "$param1",
	                       "$param2",
	                       "$param3",
	                       "$param4",
	                       "$param5",
	                       "$param6",
	                       "$param7",
	                       "$param8",
	                       "$param9",
	                       "$param10",
	                       "$param11",
	                       "$param12"
	                       ],
	                       ["$set-perfcommon-data", 
	                        "$perfnum",
	                        ["$hex", "000c"],
	                        ["$cons", 0,
	                         ["$cons", "$type",
	                          ["$cons", "$param1",
	                           ["$cons", "$param2",
	                            ["$cons", "$param3",
	                             ["$cons", "$param4",
	                              ["$cons", "$param5",
	                               ["$cons", "$param6",
	                                ["$cons", "$param7",
	                                 ["$cons", "$param8",
	                                  ["$cons", "$param9",
	                                   ["$cons", "$param10",
	                                    ["$cons", "$param11",
	                                     ["$cons", "$param12", "$nil" ]
	                                    ]
	                                   ]
	                                  ]
	                                 ]
	                                ]
	                               ]
	                              ]
	                             ]
	                            ]
	                           ]
	                          ]
	                         ]
	                        ]
	                       ]
	],
	"set-perfcommon-efx":["$lambda", 
	                      ["$perfnum", "$output", "$level", "$chorus", "$reverb", "$contsource1", "$contdepth1", "$contsource2", "$contdepth2"],
	                      ["$set-perfcommon-data",
	                       "$perfnum",
	                       ["$hex", "001a"],
	                       ["$cons", ["$cond", 
	                                  [ ["$eq", "MIX",     "$output"], 0 ],
	                                  [ ["$eq", "DIRECT1", "$output"], 1 ],
	                                  [ ["$eq", "DIRECT2", "$output"], 2 ],
	                                  [ "$T", 0]],
	                                  ["$cons", "$level",
	                                   ["$cons", "$chorus",
	                                    ["$cons", "$reverb",
	                                     ["$cons", "$contsource1",
	                                      ["$cons", "$contdepth1",
	                                       ["$cons", "$contsource2",
	                                        ["$cons", "$contdepth2", "$nil"]
	                                       ]
	                                      ]
	                                     ]
	                                    ]
	                                   ]
	                                  ]
	                       ]
	                      ]
	],
	"set-perfcommon-efx-overdrive":["$lambda", ["$perfnum", "$drive", "$outpan", "$ampsimtype", "$lowgain", "$highgain", "$outlevel"],
	    	                        ["$set-perfcommon-efx-params",
	    	                         "$perfnum",
	    	                         1, 
	    	                         "$drive", 
	    	                         "$outpan", 
	    	                         "$ampsimtype", 
	    	                         "$lowgain", 
	    	                         "$highgain", 
	    	                         "$outlevel",
	    	                         0, 
	    	                         0, 
	    	                         0, 
	    	                         0, 
	    	                         0, 
	    	                         0
	    	                         ]
	],
	"set-perfcommon-efx-distortion":["$lambda", ["$perfnum", "$drive", "$outpan", "$ampsimtype", "$lowgain", "$highgain", "$outlevel"],
	                                 ["$set-perfcommon-efx-params",
	                                  "$perfnum",
	                                  2, 
	                                  "$drive", 
	                                  "$outpan", 
	                                  "$ampsimtype", 
	                                  "$lowgain", 
	                                  "$highgain", 
	                                  "$outlevel",
	                                  0, 
	                                  0, 
	                                  0, 
	                                  0, 
	                                  0, 
	                                  0
	                                  ]
	],
	"set-perfcommon-chorus":["$lambda", ["$perfnum", "$level", "$rate", "$depth", "$predelay", "$feedback", "$output"],
	                         ["$set-perfcommon-data",
	                          "$perfnum",
	                          ["$hex", "0022"],
	                          ["$cons", "$level",
	                           ["$cons", "$rate",
	                            ["$cons", "$depth",
	                             ["$cons", "$predelay",
	                              ["$cons", "$feedback",
	                               ["$cons", "$output", "$nil"]
	                              ]
	                             ]
	                            ]
	                           ]
	                          ]
	                         ]
	],
	"set-perfcommon-reverb":["$lambda", ["$perfnum", "$type", "$level", "$time", "$hfdump", "$delayfb"],
	                         ["$set-perfcommon-data",
	                          "$perfnum",
	                          ["$hex", "0028"],
	                          ["$cons", "$type",
	                           ["$cons", "$level",
	                            ["$cons", "$time",
	                             ["$cons", "$hfdump",
	                              ["$cons", "$delayfb", "$nil"]
	                             ]
	                            ]
	                           ]
	                          ]
	                         ]
	],
	"set-perfcommon-tempo":["$lambda", ["$perfnum", "$tempo"],
	                         ["$set-perfcommon-data",
	                          "$perfnum",
	                          ["$hex", "002d"],
	                          ["$cons", ["$shift", -4, ["$&", ["$hex", "f0"], "$tempo" ] ],
	                           ["$cons", ["$&", ["$hex", "0f"], "$tempo" ], "$nil" ],
	                          ]
	                         ]
	],
	"baseaddress-perfpart":["$lambda", ["$perfnum", "$partnum"],
	                        ["$+", 
	                         ["$baseaddress-perf", "$perfnum"], 
	                         ["$*", ["$hex", "0100"], "$partnum"], 
	                         ["$hex", 1000]
	                        ]
	],
	"set-perfpart-data":["$lambda", ["$perfnum", "$partnum", "$offset", "$values"] ,
	                     ["$setdata",
	                      ["$+", "$offset", ["$baseaddress-perfpart", "$perfnum", "$partnum"]],
	                      "$values"
	                      ]
	],
	"set-perfpart-enable":["$lambda", ["$perfnum", "$partnum"],
	                       ["$set-perfpart-data", "$perfnum", "$partnum", ["$hex", "0000"], ["$cons", 1, "$nil"]]
	],
	"set-perfpart-disable":["$lambda", ["$perfnum", "$partnum"],
	                       ["$set-perfpart-data", "$perfnum", "$partnum", ["$hex", "0000"], ["$cons", 0, "$nil"]]
	],
	"set-perfpart-midich":["$lambda", ["$perfnum", "$partnum", "$channel"],
	                       ["$set-perfpart-data", "$perfnum", "$partnum", ["$hex", "0001"], ["$cons", "$channel", "$nil"]]
	],
	"set-perfpart-patch":["$lambda", ["$perfnum", "$partnum", "$patchgrouptype", "$patchgroupid", "$patchnum"],
                          ["$set-perfpart-data",
                           "$perfnum",
                           "$partnum",
                           ["$hex", "0002"], 
                           ["$cons", "$patchgrouptype", 
                            ["$cons", "$patchgroupid",
                             ["$cons", ["$shift", -4, ["$&", ["$hex", "000000f0"], "$patchnum"] ],
                              ["$cons", ["$&", ["$hex", "0000000f"], "$patchnum"] , "$nil"],
                             ] 
                            ]
                           ]
                          ]
	],
	"set-perfpart-level":["$lambda", ["$perfnum", "$partnum", "$level"],
	                  
	                      ["$set-perfpart-data", 
	                        "$perfnum", 
	                        "$partnum", 
	                        ["$hex", "06"], 
	                        "$level"
	                       ]
	],
	"set-perfpart-pan":["$lambda", ["$perfnum", "$partnum", "$pan"],
	                       ["$set-perfpart-data", 
	                        "$perfnum", 
	                        "$partnum", 
	                        ["$hex", "07"], 
	                        ["$quote", "$pan"]
	                       ]
	],
	"set-perfpart-reverb":["$lambda", ["$perfnum", "$partnum", "$reverb"],
	                       ["$set-perfpart-data", 
	                        "$perfnum", 
	                        "$partnum", 
	                        ["$hex", "0d"], 
	                        ["$quote", "$reverb"]
	                       ]
	],
	"set-perfpart-chorus":["$lambda", ["$perfnum", "$partnum", "$chorus"],
	                       ["$set-perfpart-data", 
	                        "$perfnum", 
	                        "$partnum", 
	                        ["$hex", "0c"], 
	                        ["$quote", "$chorus"]
	                       ]
	],
	"set-perfpart-mixefx-send-level":["$lambda", ["$perfnum", "$partnum", "$efx"],
	                          ["$set-perfpart-data", 
	                           "$perfnum", 
	                           "$partnum", 
	                           ["$hex", "0b"], 
	                           ["$cons", "$efx", "$nil"]
	                          ]
	],
	"set-perfpart-chorus-send-level":["$lambda", ["$perfnum", "$partnum", "$efx"],
	    	                          ["$set-perfpart-data", 
	    	                           "$perfnum", 
	    	                           "$partnum", 
	    	                           ["$hex", "0c"], 
	    	                           ["$cons", "$efx", "$nil"]
	    	                          ]
    ],
	"set-perfpart-reverb-send-level":["$lambda", ["$perfnum", "$partnum", "$efx"],
	    	                          ["$set-perfpart-data", 
	    	                           "$perfnum", 
	    	                           "$partnum", 
	    	                           ["$hex", "0d"], 
	    	                           ["$cons", "$efx", "$nil"]
	    	                          ]
    ],
	"set-perfpart-output-level":["$lambda", ["$perfnum", "$partnum", "$volume"],
	                             ["$set-perfpart-data", 
	                              "$perfnum", 
	                              "$partnum", 
	                              ["$hex", "06"], 
	                              ["$cons", "$volume", "$nil"]
	                             ]
	],
	"set-perfpart-output-assign":["$lambda", ["$perfnum", "$partnum", "$dest"],
	                              ["$set-perfpart-data", 
	                               "$perfnum", 
	                               "$partnum", 
	                               ["$hex", "000a"], 
	                               ["$cons",
	                                ["$cond", 
	                                 [ ["$eq", "MIX",     "$dest"], 0 ],
	                                 [ ["$eq", "EFX",     "$dest"], 1 ],
	                                 [ ["$eq", "DIRECT1", "$dest"], 2 ],
	                                 [ ["$eq", "DIRECT2", "$dest"], 3 ],
	                                 [ ["$eq", "PATCH",   "$dest"], 4 ],
	                                 [ "$T", 0]
	                                ],
	                                "$nil"
	                                ]
	                              ]
	],
	"jv-set-currentperf":["$lambda", ["$perfnum"],
	                      ["$let", [["$dmy", "$nil"]],
	                       ["$setq", "$currentperf", "$perfnum"],
	                       "$nil"
	                       ]
	],
	"jv-init-part":["$lambda", ["$partnum", "$channel"],
	                      ["$let", [["$dmy", "$nil"]],
	                       ["$set-perfpart-enable",  "$currentperf", "$partnum"],
	                       ["$set-perfpart-midich",  "$currentperf", "$partnum", "$channel"],
                           ["$set-perfpart-data",    "$currentperf", "$partnum", ["$hex", "000e"], ["$cons", 1, "$nil"]],
                           ["$set-perfpart-data",    "$currentperf", "$partnum", ["$hex", "000f"], ["$cons", 1, "$nil"]],
                           ["$set-perfpart-data",    "$currentperf", "$partnum", ["$hex", "0010"], ["$cons", 1, "$nil"]],
                           ["$set-perfpart-data",    "$currentperf", "$partnum", ["$hex", "0011"], ["$cons", 1, "$nil"]],
                           ["$set-perfpart-data",    "$currentperf", "$partnum", ["$hex", "0012"], ["$cons", 127, "$nil"]],
	                       "$nil"
	                       ]
	],
	"jv-refresh-currentperf":["$lambda", [],
	                   ["$set-syscommon-perfnum", "$currentperf"],
	],
	"jv-set-patchnum-user":["$lambda", ["$partnum", "$patchnum"],
	                        ["$set-perfpart-patch", "$currentperf", "$partnum", 0, 1, "$patchnum"]
	],
	"jv-set-patchnum-card":["$lambda", ["$partnum", "$patchnum"],
	                    ["$set-perfpart-patch", "$currentperf", "$partnum", 0, 2, "$patchnum"]
	],
	"jv-set-patchnum-pr-a":["$lambda", ["$partnum", "$patchnum"],
	                    ["$set-perfpart-patch", "$currentperf", "$partnum", 0, 3, "$patchnum"]
	],
	"jv-set-patchnum-pr-b":["$lambda", ["$partnum", "$patchnum"],
	                    ["$set-perfpart-patch", "$currentperf", "$partnum", 0, 4, "$patchnum"]
	],
	"jv-set-patchnum-pr-c":["$lambda", ["$partnum", "$patchnum"],
	                    ["$set-perfpart-patch", "$currentperf", "$partnum", 0, 5, "$patchnum"]
	],
	"jv-set-patchnum-pr-d":["$lambda", ["$partnum", "$patchnum"],
	                    ["$set-perfpart-patch", "$currentperf", "$partnum", 0, 6, "$patchnum"]
	],
	"jv-set-patchnum-pr-e":["$lambda", ["$partnum", "$patchnum"],
	                    ["$set-perfpart-patch", "$currentperf", "$partnum", 0, 7, "$patchnum"]
	],
	"jv-set-patchnum-pcm":["$lambda", ["$partnum", "$patchnum"],
	                   ["$set-perfpart-patch", "$currentperf", "$partnum", 1, 1, "$patchnum"]
	],
	"jv-set-patchnum-xp-pop":["$lambda", ["$partnum", "$patchnum"],
		                       ["$set-perfpart-patch", "$currentperf", "$partnum", 2, 1, "$patchnum"]
	],
	"jv-set-patchnum-xp-orchestra":["$lambda", ["$partnum", "$patchnum"],
		                       ["$set-perfpart-patch", "$currentperf", "$partnum", 2, 2, "$patchnum"]
	],
	"jv-set-patchnum-xp-piano":["$lambda", ["$partnum", "$patchnum"],
		                       ["$set-perfpart-patch", "$currentperf", "$partnum", 2, 3, "$patchnum"]
	],
	"jv-set-patchnum-xp-vintagesynth":["$lambda", ["$partnum", "$patchnum"],
		                       ["$set-perfpart-patch", "$currentperf", "$partnum", 2, 4, "$patchnum"]
	],
	"jv-set-patchnum-xp-world":["$lambda", ["$partnum", "$patchnum"],
		                       ["$set-perfpart-patch", "$currentperf", "$partnum", 2, 5, "$patchnum"]
	],
	"jv-set-patchnum-xp-supersoundset":["$lambda", ["$partnum", "$patchnum"],
			                       ["$set-perfpart-patch", "$currentperf", "$partnum", 2, 7, "$patchnum"]
	],
	"jv-set-patchnum-xp-keyboards6070s":["$lambda", ["$partnum", "$patchnum"],
			                       ["$set-perfpart-patch", "$currentperf", "$partnum", 2, 8, "$patchnum"]
	],
	"jv-set-patchnum-xp-session":["$lambda", ["$partnum", "$patchnum"],
	                          ["$set-perfpart-patch", "$currentperf", "$partnum", 2, 9, "$patchnum"]
	],
	"jv-set-patchnum-xp-bassanddrums":["$lambda", ["$partnum", "$patchnum"],
			                       ["$set-perfpart-patch", "$currentperf", "$partnum", 2, 10, "$patchnum"]
	],
	"jv-set-patchnum-xp-techno":["$lambda", ["$partnum", "$patchnum"],
	                         ["$set-perfpart-patch", "$currentperf", "$partnum", 2, 11, "$patchnum"]
	],
	"jv-set-patchnum-xp-hiphop":["$lambda", ["$partnum", "$patchnum"],
	                         ["$set-perfpart-patch", "$currentperf", "$partnum", 2, 12, "$patchnum"]
	],
	"jv-set-patchnum-xp-vocal":["$lambda", ["$partnum", "$patchnum"],
		                         ["$set-perfpart-patch", "$currentperf", "$partnum", 2, 13, "$patchnum"]
	],
	"jv-set-patchnum-xp-asia":["$lambda", ["$partnum", "$patchnum"],
		                         ["$set-perfpart-patch", "$currentperf", "$partnum", 2, 14, "$patchnum"]
	],
	"jv-set-patchnum-xp-specialfx":["$lambda", ["$partnum", "$patchnum"],
		                         ["$set-perfpart-patch", "$currentperf", "$partnum", 2, 15, "$patchnum"]
	],
	"jv-set-patchnum-xp-orchestra2":["$lambda", ["$partnum", "$patchnum"],
		                         ["$set-perfpart-patch", "$currentperf", "$partnum", 2, 16, "$patchnum"]
	],
	"jv-set-patchnum-xp-country":["$lambda", ["$partnum", "$patchnum"],
			                         ["$set-perfpart-patch", "$currentperf", "$partnum", 2, 17, "$patchnum"]
	],
	"jv-set-patchnum-xp-latin":["$lambda", ["$partnum", "$patchnum"],
			                         ["$set-perfpart-patch", "$currentperf", "$partnum", 2, 18, "$patchnum"]
	],
	"jv-set-patchnum-xp-house":["$lambda", ["$partnum", "$patchnum"],
			                         ["$set-perfpart-patch", "$currentperf", "$partnum", 2, 19, "$patchnum"]
	],
	"jv-set-output-level":["$lambda", ["$partnum", "$level"],
		        		      ["$set-perfpart-output-level", "$currentperf", "$partnum", "$level"]
	],
	"jv-set-output-assign":["$lambda", ["$partnum", "$output"],
      			              ["$set-perfpart-output-assign", "$currentperf", "$partnum", "$output"]
	],
	"jv-set-mixefx-send-level":["$lambda", ["$partnum", "$level"],
			              ["$set-perfpart-mixefx-send-level", "$currentperf", "$partnum", "$level"]
	],
	"jv-set-chorus-send-level":["$lambda", ["$partnum", "$level"],
			              ["$set-perfpart-chorus-send-level", "$currentperf", "$partnum", "$level"]
	],
	"jv-set-reverb-send-level":["$lambda", ["$partnum", "$level"],
			              ["$set-perfpart-reverb-send-level", "$currentperf", "$partnum", "$level"]
	]
}
