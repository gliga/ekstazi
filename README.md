Ekstazi
=======

<img src="http://ekstazi.org/Ekstazi.png" alt="Ekstazi" width="238" height="224"> 

Ekstazi is a tool for optimizing software testing. Ekstazi implements
Regression Test Selection (RTS) for Java, which is a technique to
detect a set of tests that can be skipped after a code change.

## Build

If you have Maven, building Ekstazi is trivial:
```
mvn install
```

## Usage

At this point, most of the instructions are located on the official
[Ekstazi web page](http://ekstazi.org/).

## Versioning

The table below shows the known working versions.

| Java version | Maven version | Ekstazi SHA |
| ------------ | ------------- | ----------- |
| 8            | [3.5.2](https://archive.apache.org/dist/maven/maven-3/3.5.2/binaries/apache-maven-3.5.2-bin.tar.gz) | 29037145 |

## Acknowledgments

The Ekstazi Logo was designed by [Vladimir
Petrovic](https://rs.linkedin.com/in/vladimirpetrovicdesign).  We also
thank Aleksandar Milicevic for his feedback on this work and many
members of the open-source community (especially Apache Commons Math
developers, Apache CXF developers, and Apache Caml developers) for
their willingness to communicate with us on topics related to Ekstazi.

## Research

If you have used Ekstazi in a research project, please cite
the research paper in any related publication:

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
<milos.gligoric@gmail.com>.
