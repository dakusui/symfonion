# What is Symfonion
Symfonion is a modern music macro language processor.

# Installing Symfonion
## Prerequisites
1. Your system has JRE 1.7 or later
2. Your system can play a midi file through Java's API

## Linux and Mac users
1. Download a .zip file symfonion-VERSION-release.zip from [here](https://github.com/dakusui/symfonion/releases/)
2. Unzip the contents. And place ```symfonion.jar``` and ```symfonion``` somewhere on your path.
3. Done

You will be able to run ```symfonion``` by typing

```
$ symfonion
```

## Windows users
1. Download a .zip file symfonion-VERSION-release.zip from [here](https://github.com/dakusui/symfonion/releases/)
2. Unzip the contents and place ```symfonion.jar``` somewhere visible to you.
3. Done.

You will be able to run ```symfonion``` by double clicking ```symfonion.jar```.

# How to run Symfonion #
By typing a command line below, ```symfonion``` will compile the given JSON file and play it.

```
$ symfonion infile
```

where "infile" is a ```symfonion``` file and it will look like this.

```json

    {
        "$parts":{ "pianor": {"$channel":0} },
        "$patterns":{
            "01r":{
                "$body":["r4","B","A","G#","A"],
                "$length":16
            },
            "02r":{
                "$body":["C>8","r8","D>","C>","B","C>", "E>8","r8","F>","E>","D#>","E>"],
                "$length":16
            },
            "04r":{
                "$body":["B>","A>","G#>","A>","B>","A>","G#>","A>", "C>>4","A>8","C>>8"],
                "$length":16
            },
            "06r":{
                "$body":[ "B>",  "F#A>", "EG>", "F#A>", "B>",  "F#A>", "EG>", "F#A>" ],
                "$length":8, "$gate":0.3
            },
            "08r":{
                "$body":["B>","F#A>","EG>","D#F#>", "E4", "r4" ],
                "$length":8, "$gate":0.3
            }
        },
        "$sequence":[
            { "$parts":{"pianor":["01r"]},   "$beats":"2/4" },
            { "$parts":{"pianor":["02r"]},   "$beats":"4/4" },
            { "$parts":{"pianor":["04r"]},   "$beats":"4/4" },
            { "$parts":{"pianor":["06r"]},   "$beats":"4/4" },
            { "$parts":{"pianor":["08r"]},   "$beats":"4/4" }
        ]
    }
```
(W.A. Mozart, K.311)

The syntax of it is described in [Syntax](src/site/asciidoc/SYNTAX.adoc)

For the detail of the command line options, please refer to [Command line manual](src/site/asciidoc/CLI.adoc)

For the updates, best practices, how-tos, examples, and so on, please visit
[Symfonion Blog](http://symfonion.hatenadiary.jp/) (mainly in Japanese)

# Copyright and license #

Copyright 2013 Hiroshi Ukai.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
