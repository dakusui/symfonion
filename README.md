# What is SyMFONION #
SyMFONION is a modern music macro language processor.

# Installing SyMFONION #
## Debian and Ubuntu users ##
Please download a .deb file from here
[.deb file](https://s3-ap-northeast-1.amazonaws.com/symfonion/symfonion_0.8.9-1_all.deb)

And type the command below.
```
$ sudo dpkg -i symfonion_VERSION_all.deb
```
You will be able to run SyMFONITON by just typing 'symfonion' command from a shell.

## Other users ##
Please download a .jar file from here
[executable jar file](https://s3-ap-northeast-1.amazonaws.com/symfonion/symfonion-0.8.9.jar)

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

"infile" is a SyMFONION file and the syntax of it is described here 
[Syntax](https://github.com/dakusui/symfonion/wiki/Syntax)

For the detail of the command line options, please refer to 
[Command line manual](https://github.com/dakusui/symfonion/wiki/Command-line-manual)

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
