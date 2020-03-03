#Synthesis of winning strategies for board games

* Insert introduction to project here *

##Project Setup:

1. Install [Java 8](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html) (Might work with newer versions but untested)
2. Install [Maven 3.6.3](https://maven.apache.org/download.cgi) (Likely to work with older/newer versions)
3. Open the Terminal and navigate to project root
4. Run `mvn verify`
..* Check that build was run successfully and the */target* folder is created in the project root

## Project Execution

Project consists of 3 different programs:
- Tic Tac Toe
- Kulibrat
- GGP-FFT

The first two programs offers GUI's for the respective games where it is possible to play around and test various strategies with FFT's
The last program offers no GUI and simply synthesizes a game winning strategy for the game specificed in the config file

These programs can be run with the following commands:
1. `mvn exec:java@tictactoe` *or simply* `mvn exec:java`
2. `mvn exec:java@kulibrat`
3. `mvn exec:java@GGP-FFT`

