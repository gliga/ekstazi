Tests to prevent runs failing with Ekstazi due to bug in Oracle Java
compile.

This test was discovered with Commons Math.  Thanks Luc!  The
following sequence of command discovered bug:

$ git clone https://github.com/apache/commons-math.git
$ cd commons-math
$ git checkout 09129d536726fac7f94f6b641e34a34a49c9a012
# Integrate Ekstazi 4.4.0 in pom.xlm.
$ mvn test -Dtest=FunctionUtilsTest.java -Pekstazi clean site -DfailIfNoTests=false
