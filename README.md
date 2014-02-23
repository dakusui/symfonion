# What is SyMFONION #
SyMFONION is a modern music macro language processor.

# Installing SyMFONION #
## Debian and Ubuntu users ##
Please download a .deb file from [here](https://s3-ap-northeast-1.amazonaws.com/symfonion/symfonion_0.8.10-1_all.deb)

And type the command below.
```
$ sudo dpkg -i symfonion_VERSION_all.deb
```
You will be able to run SyMFONITON by just typing 'symfonion' command from a shell.

## Other users ##
Please download a .jar file from [here](https://s3-ap-northeast-1.amazonaws.com/symfonion/symfonion-0.8.10.jar)

You will be able to run SyMFONION by using java command from a shell.

```
$ java -jar symfonion-VERSION.jar infile
```

# How to run SyMFONION #
By typing a command line below, symfonion will compile the given JSON file and play it.

```
$ symfonion infile
```

or

```
$ java -jar  symfonion-VERSION.jar infile
```

"infile" is a SyMFONION file and it will look like this.
```javascript
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
        { "$patterns":{"pianor":["01r"]},   "$beats":"2/4" },
        { "$patterns":{"pianor":["02r"]},   "$beats":"4/4" },
        { "$patterns":{"pianor":["04r"]},   "$beats":"4/4" },
        { "$patterns":{"pianor":["06r"]},   "$beats":"4/4" },
        { "$patterns":{"pianor":["08r"]},   "$beats":"4/4" }
    ]
}
```
(W.A. Mozart, K.311)

The syntax of it is described  
[Syntax](SYNTAX.md)

For the detail of the command line options, please refer to 
[Command line manual](CLI.md)

For the updates, best practices, how-tos, examples, and so on, please visit
[Symfonion Blog](http://symfonion.hatenadiary.jp/)

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
