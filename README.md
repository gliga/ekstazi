Ekstazi
=======

Ekstazi is a tool for optimizing software testing. Ekstazi implements
Regression Test Selection (RTS) for Java, which is a technique to
detect a set of tests that can be skipped after a code change.

<img src="http://ekstazi.org/Ekstazi.png" alt="Ekstazi" width="238" height="224"> 

## Developing

### Build

If you have Maven, building Ekstazi is trivial:

```
JAVA_TOOL_OPTIONS="-Djdk.attach.allowAttachSelf=true" mvn install
```

Building Ekstazi targets Java 8 bytecode specification. The following
Java versions were used successfully to build Ekstazi code.

| Ekstazi SHA | Java | Maven |
| ----------- | ---- | ----- |
| bf330fae    | 8; 11; 17 | 3.5.2 |

## Use

At this point, most of the instructions are located on the official
[Ekstazi web page](http://ekstazi.org).

The table below shows the known working combination.  (Although we
focus on Java LTS versions, other versions might work as well.)

| Ekstazi SHA | Java | Maven | JUnit |
| ----------- | ---- | ----- | ----- |
| bf330fae    | 8; 11:eight_pointed_black_star:; 17:eight_pointed_black_star: | 3.5.2 | 3.8.2; 4.10; 4.13.2 |

:eight_pointed_black_star: Running these configuration requires
setting an extra option on command line:
```
JAVA_TOOL_OPTIONS="-Djdk.attach.allowAttachSelf=true" mvn ...
```

## Acknowledgments

The Ekstazi Logo was designed by [Vladimir
Petrovic](https://rs.linkedin.com/in/vladimirpetrovicdesign).  We also
thank Aleksandar Milicevic for his feedback on this work and many
members of the open-source community (especially Apache Commons Math
developers, Apache CXF developers, and Apache Caml developers) for
their willingness to communicate with us on topics related to Ekstazi.

## Research

If you have used Ekstazi in a research project, please cite the
following
[paper](https://users.ece.utexas.edu/~gligoric/papers/GligoricETAL15Ekstazi.pdf):

```bibtex
@inproceedings{GligoricETAL15Ekstazi,
  author = {Gligoric, Milos and Eloussi, Lamyaa and Marinov, Darko},
  title = {Practical Regression Test Selection with Dynamic File Dependencies},
  booktitle = {International Symposium on Software Testing and Analysis},
  pages = {211--222},
  year = {2015},
}
```

## Contact

Feel free to get in touch if you have any comments: Milos Gligoric
`<milos.gligoric@gmail.com>`.
