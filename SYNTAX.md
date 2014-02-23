# SyMFONION Modern Music Macro Language #

"SyMFONION" is a JSON-based music macro language which enables you to describe a piece of music as easily as 8-bit days' MML but in much more structured way.

Here is an example of a piece of music written in SyMFONION.

```javascript
    {
      "$parts":{
        "testviolin":{"$channel":0}
      },
      "$patterns":{
        "test1":{
          "$body":[
            {"$notes":"C", "$length":"2", "$pan":127, "$program":2},
            {"$notes":"C", "$length":"2", "$pan":0}
          ],
          "$length":"8"
        }
      },
      "$sequence":[
        {
          "$beats":"8/8",
          "$patterns":{
            "testviolin":["test1"]
          }
        },
        {
          "$beats":"8/8",
          "$patterns":{
            "testviolin":["test1"]
          }
        }
      ]
    }
```

Basically, a SyMFONION program is a JSON object and its three most important elements are, "$parts", "$patterns", and "$sequence".

A value for "$parts" is a dictionary which describes 'parts' in music score (like 'piano part', 'guitar part', and so on). "$patterns" holds a set of patterns which are melodies, rhythm patterns, effect patters, and so on. And "$sequence" organises how each pattern should be played. For instance, a pattern "test1" is played twice on a part "testviolin" in the example above.

# Parts #
By using 'parts' element you can specify which part uses which midi channel. In the example below, the part 'test' will use the midi channel '0'. Note that in symfonion language, A channel number is 0 origin.

```javascript
      "$parts":{
        "test":{"$channel":0},
      },
```

If you want to use external MIDI devices, you need to specify "$port" attribute for each part,

```javascript
    "$parts":{
       "test":{ "$channel":0, "$port":"part1" }
    }
```

Part names must be defined through system properties. For more detail, please refer to [[Command line parameters]].

# Notemaps #
"Notemaps" are a bit advanced feature. By using this feature, users can configure a drum map for a pre-General MIDI age's synthesizer (Yamaha SY-77 for instance).
(t.b.d.)


# Patterns #
"Patterns" section is a dictionary and defines patterns which are referred to from inside "Sequence" section.

```javascript
    "$patterns":{
      "test1":{
        "$body":[
          {"$notes":"C", "$length":"2", "$pan":127, "$program":2},
          {"$notes":"C", "$length":"2", "$pan":0}
        ],
        "$length":"8"
      }
    },
```

Each key of this dictionary is a name of a pattern. And the key is associated with a dictionary which describes the pattern. Generally speaking a pattern is a sequence of notes, which defines something like melodies, short rhythms, and so on. 

A pattern dictionary can have some keys, which are "$notemap", "$body", and base parameters like "$length", "$velocitybase", "$velocitydelta", "$length", and so on.

## Notemap ##

A user can set this attribute to a name defined in the section "$notemaps" or predefined note map name.

There are two pre-defined note maps, which are "$normal" and "$percussion". "$normal" is a normal note map where "C" is mapped to 60 and "D" to 62. "$percussion" is configured for drum kits defined in General MIDI standard.

## Body ##
A "$body" of a pattern is a list of 'strokes' and each stroke is a dictionary like this.

```javascript
    "$body":[
      {"$notes":"C", "$length":"2", "$pan":127, "$program":2},
      {"$notes":"C", "$length":"2", "$pan":0},
    ],
```

Relationships between patterns, strokes, and notes are described in the figure below.
 
```bash
       +-------+   body+----------------+1     n+----------+
       |Pattern|<>-+-->|Stroke          |<>---->|  Note    |
       +-------+1  |  n+----------------+       +----------+
                   |   |volume:int[]    |       |int key   |
                   |   |pan:int[]       |       |int accent|
                   |   |reverb:int[]    |       +----------+
                   |   |chorus:int[]    |
                   |   |pitch:int[]     |
                   |   |modulation:int[]|
                   |   |program:int     |
                   |   |tempo:int       |
                   |   +----------------+
                   |
                   |   +----------+
                   +-->|Parameters|
    defaultParameters 1+----------+
```

A stroke is a set of Midi messages which are transmitted to a single midi channel of a midi device during a note (or notes belongs to one chord in a score) is being played.

Midi messages in SyMFONION can be divided into two groups. One is 'note' messages (or simply "notes") and the other is 'non-note' messages.
There are several types of non-note messages, which are 'volume, 'pan', 'reverb', 'chorus', 'pitch', and so on.
These are played as independent messages on a midi device.

On the other hand, there are some parameters which directly belong to note messages such as velocity and length. And these parameters modify the note messages directly.
In the example below, "$velocitybase" is a parameter which modifies a note and "$volume" is a non note message which is translated into a control change message (#7).

```javascript
    {
      "$notes":"C",
      "$velocitybasse":100,
      "$volume":88,
    }
```

But in terms of SyMFONION syntax, users can not tell which attributes are parameters for a note and which are non-note messages. But the developer of SyMFONION thought that it is not important for users and SyMFONION should abstract the midi message/event structure. In other words, users do not need to know if an attribute is a note parameter or a non-note message.

### Notes ###
Notes in a stroke must match a regular expression pattern defined by a string below. (This is a "Java-style" regular expression and its syntax slightly different from other ones such as perl's. Refer to this document. http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html)

```java
    "([A-Zac-z])([#b]*)([><]*)([\\+\\-]*)"
```

For example, strings below are valid for this attribute.

```java
    "C";                  // Translated to "C3" (Note number 60)
    "D#";                 // "D sharp" (Note number 63)
    "D##";                // "D doublesharp" (Note number 64)
    "Eb";                 // "E flat" (Note number 63)
    "Ebb";                // "E doubleflat" (Note number 62)
    "C>";                 // "C4" (Note number 72)
    "C>>";                // "C5" (Note number 84)
    "C<";                 // "C2" (Note number 48)
    "C<<";                // "C1" (Note number 36)
    "C+";                 // "C3" but velocity will be velocitybase + velocitydelta
    "C++";                // "C3" but velocity will be velocitybase + velocitydelta * 2
    "C-";                 // "C3" but velocity will be velocitybase - velocitydelta
    "C--";                // "C3" but velocity will be velocitybase - velocitydelta * 2
    "C>#+";               // You can use ">", "#", and "+" (and other modifiers) in combination.
    "CEG";                // Chord C (C3, E3, and G3 will be played at once.)
    "C#>>+++E#++G#+";     // You can also use modifier in combination even when you are writing a chord.
```
    
### Non-note messages ###
Some of non-note messages, for example '$volume', can have arrays as their values.

```javascript
    {
        "$notes":"C",
        "$volume":[0,10,20,40]
    }
```

If this stroke is a quarter note, 4 volume messages (control change #7) each of whose length is equal to sixteenth note are sent one after another. The values of of the messages will be 0, 10, 20, and 40.

You can omit values in between concrete values like this,

```javascript
    {
        "$notes":"C",
        "$volume":[0,,,40]
    }
    {
        "$notes":"C",
        "$volume":[0,,,80,,,100]
    },
```

SyMFONION fills the gap by using linearization.
And if you give an integer to "$volume" attribute, its considered as an array which has only one value. In other words, strokes in the example below are equivalent to each other.

```javascript
    {
        "$notes":"C",
        "$volume":80
    }
    {
        "$notes":"C",
        "$volume":[80]
    },
```

This feature is called "arrayable" and users can use this features for "$volume", "$pan", "$reverb", and so on.

#### Volume ####
This feature is 'arrayable'.

```javascript
    {
        "$notes":"C",
        "$volume":[0,,,70,,,80],
    }
```

Volume change messages (control change #7) are sent with given values to the channel with which this pattern is associated through a part.


#### Pan ####
This feature is 'arrayable'.

```javascript
    {
        "$notes":"C",
        "$pan":[0,,,,,,127],
    }
```

Pan change messages (control change #10) are sent with given values to the channel with which this pattern is associated through a part.

#### Reverb ####
This feature is 'arrayable'.

```javascript
    {
        "$notes":"C",
        "$reverb":[0,,,,,,127],
    }
```

Reverb change messages (control change #91) are sent with given values to the channel with which this pattern is associated through a part.

#### Chorus ####
This feature is 'arrayable'.

```javascript
    {
        "$notes":"C",
        "$chorus":[0,,,,,,127],
    }
```

Chorus change messages (control change #93) are sent with given values to the channel with which this pattern is associated through a part.

#### Pitch ####
This feature is 'arrayable'.

```javascript
    {
        "$notes":"C",
        "$pitch":[0,,,,,,127],
    }
```

Pitch bend messages are sent with given values to the channel with which this pattern is associated through a part.

127 maximum upward bend / 64 = nobend / 0 maximum downward bend.

#### Modulation ####
This feature is 'arrayable'.

```javascript
    {
        "$notes":"C",
        "$modulation":[0,,,,,,127],
    }
```

Modulation wheel messages (control change #1) are sent with given values to the channel with which this pattern is associated through a part.

#### Program ####
This feature is NOT 'arrayable'.

```javascript
    {
        "$notes":"C",
        "$program":0,
    }
```

A program change message is sent with a given value to the channel with which this pattern is associated through a part.

#### Tempo ####
This feature is NOT 'arrayable'.

```javascript
    {
        "$notes":"C",
        "$tempo":180,
    }
```

A tempo change meta message is sent with a given value to the channel with which this pattern is associated through a part.

Note that this affects all the tracks in the sequence being played.

#### Sysex ####
By using $sysex attribute users can send 'System exclusive' messages to a midi device.
The value is a JSON array which comply with 'LogiasLisp' syntax.
For more details, refer to [Midi System Exclusive messages] (t.b.d.).


### Note parameters ###
Notes are also represented by midi messages in midi device layer. 
Actually, one note consists of two messages, one is "note on" and the other is "note off".

And usually the time between note on and note off is slight shorter than the time calculated from the tempo and the length of the note. The time between note-on and note-off is usually called 'gate-time'.

```
       Note-on     Note-off
          
         | gate time  |     |
         |<---------->|     |
         | note length|     |
         |<---------------->|
         |            |     |
        _|            |     |
       <_>            |     |
```



Each note-on message has 'velocity' value. Velocity of a note message represents how 'strong' the note should be played. If a note has a larger velocity value, it will be played louder by a synthesizer. (Modern synthesizers changes not only the volume but also tone and other features of the note.)


#### Length ####
"$length" is a string/int value which defines the note length.
Both of below are the same meanin
g.

```javascript
    {"$notes":"C", "$length":"8"},
    {"$notes":"C", "$length":8},
```

But to create a dotted note, you can only use a string for "$length"

```javascript
    {"$notes":"C", "$length":"8."},   // dotted eighth note.
```

Also you can write double dotted/triple dotted notes by using a string.

```javascript
    {"$notes":"C", "$length":"8.."},  // double dotted eighth note.
```

```javascript
    {"$notes":"C", "$length":"8..."}, // triple dotted eighth note.
```

The default value is "4", which means a quarter note.

#### Velocity base, velocity delta ####
"$velocitybase" is an integer which specifies the velocity value of notes if they have no accent sign ("+" and "-").

And "$velocitydelta" is also an integer which specifies the value one accent sign ("+" and "-") is equal to.

So, the velocity value set to midi messages can be calculated by the formula below,

```
    velocity value = $velocitybase + (  (number of "+" in notes)
                                      - (number of "-" in notes) ) * $velocitydelta
```

The default value of $velocitybase is 64 and $velocitydelta is 10

#### Gate ####
In SyMFONION, The note parameter "$gate" is a float value which signifies the ratio of the gate time to the note length.

The default value is 0.8.

#### Transpose ####
All notes have their own number. For instance, C3 (The center "C") is 60.
Users can transpose the notes by using this feature.

```
    {"$notes":"C", "$transpose":1},
```

The example above will be played as note 61 (=C3 +1 = C3#)
Negative values are also allowed.

This parameter is not very useful when you use this with notes. This parameter should be typically used directly under a pattern definition dictionary which is discussed later.

the default value is 0.

## Defining default values within a pattern ##

Since it is painful to write $gate or $velocitybase every time for each note, users can define the default values in a pattern. Of course these default values can be overridden by each note.

As discussed so far, users can write a symfonion file like below.

```javascript
    "patternexample":{
        "$body":[
            {
              "$notes":"CEG",
              "$length":"8",
              "$gate":"0.5"
            },
            {
              "$notes":"CEG",
              "$length":"8",
              "$gate":"0.5"
            },
                     :
                     :
            {
              "$notes":"CEG",
              "$length":"8",
              "$gate":"0.5"
            }
        ]
    }
```

By using this feature, the pattern above can be rewritten like this,

```javascript
    "patternexample":{
        "$body":[
            { "$notes":"CEG" },
            { "$notes":"CEG" },
                     :
                     :
            { "$notes":"CEG" }
        ],
        "$length":"8",
        "$gate":"0.5"
    }
```

Users can write note parameters directly under "pattern definitions" (in the case above "pattern example" is a "pattern definition, for example) and they are in effect all the notes in "$body" attribute which is in the same pattern.

# Groove #
In real musical works, all the sixteenth notes have neither the same length nor the same strength. One comes at the beginning of a bar usually longer and stronger than the others usually. And if there are 4 forth notes in a bar, second and forth ones are shorter and weaker than the others. These fluctuations are called 'grooves' in DTM world.

In symfonion syntax, you can define grooves like below,

```javascript
    "$grooves":{
        "16beats":[
            { "$length":"16", "$ticks":28, "$accent":30 },
            { "$length":"16", "$ticks":20, "$accent":-10 },
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
```

In this example, there are 16 sixteenth notes each of which has independent "$ticks" and "$accent".
If a pattern is played with this groove, the first sixteen note will be lengthen 28 MIDI ticks, while usually sixteenth notes have only 24 MIDI ticks (currently, a whole note is fixed to 384 MIDI ticks in symfonion). And 30 is added to the original velocity of the note.


# Sequence #
In the sequence section, users can organize patterns into one piece of music.
The value of the "$sequence" attribute is a list of dictionaries whose members are "$beats" and "$patterns".
And each dictionary represents a bar in a score.


```javascript
      "$sequence":[
        {
          "$beats":"8/8",
          "$patterns":{
            "test":["test1"],
          }
        },
        {
          "$beats":"8/8",
          "$patterns":{
            "test":["test1"],
          }
        },
      ],
```

## Beats ##
"$beats" specifies a length of the bar. Users can give a fraction as a string, like "4/4", "3/4", "16/16", and so on.

The SyMFONION uses this information to determine the length of the bar. Therefore "8/8", "4/4", and "16/16" are considered to be completely the same thing. 

The end of the bar is given by "$beats". If a pattern in the bar is longer the length given by "$beats", notes after the end  of the bar will not be played.

The default value is "4/4".

## Patterns ##
"$patterns" in "$sequence" section are dictionaries whose keys are part names which are defined in "$parts" section. 
The value for the key is a list whose members are names of patterns for the part.
The names of the patterns must be defined in "$patterns" section (see "Patterns" section).

A part usually corresponds to an instrument in the real world. Then, why do we have a list for a part?

The reason is because the designer of SyMFONION wanted to allow users to overlay multiple patterns. 

For example, users of SyMFONION may want to write "Fade-in" or "Fade-out" for a certain part in the sequence.

In that case, what users need to do is below,

At first, define "fade-in" or "fade-out" pattern in "$patterns" section.

```javascript
    "$patterns":{
        "fade-in":{
            "$length":1,
            "$volume":[0,,,,,,,,,,,127],
        },
        "fade-out":{
            "$length":1,
            "$volume":[127,,,,,,,,,,,0],
        },
        "melody":{
            ....
        },
    }
```

Now users can write a sequence with fade-in and out.

```javascript
    "$sequence":[
        {
            "piano":["fade-in", "melody"],
        },
        {
            "piano":["melody"],
        },
        {
            "piano":["fade-out", "melody"],
        },
    ]
```

In order to set groove to be used in a sequence, users need to set "$groove" attribute for a pattern,

```javascript
    {
            "$beats":"16/16",
            "$patterns":{
                "vocal":["melody1"]
            },
            "$groove":"16beats"
    },
```

If the groove is not defined in "$grooves" section, an error will be reported.