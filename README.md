# Synthesis of human-friendly winning strategies for board games

Insert introduction to project here

## Project Setup:

1. Install Java8 or newer
2. Install [Maven 3](https://maven.apache.org/download.cgi)
3. Open the Terminal and navigate to project root
4. Run `mvn verify`
   - Check that build was run successfully and the */target* folder is created in the project root

## Project Execution

Project consists of 5 different programs:
- Tic-tac-toe
- Sim
- Men's Morris (Six or 3)
- Kulibrat
- General Game Playing (GGP)

Tic-tac-toe and Kulibrat offers GUI's for the respective games where it is possible to play around and test various strategies with FFT's

These programs can be run with the following commands:
1. `mvn exec:java@tictactoe` *or simply* `mvn exec:java`
2  `mvn exec:java@sim` 
3  `mvn exec:java@morris`
4. `mvn exec:java@kulibrat`
5. `mvn exec:java@ggp`

## Configuration

There are 6 configuration files in total that can alter the way the strategy is generated. There is a file for each type of program plus a global config file, which are called:
1. *global.properties*: This is the main configuration file which contains options such as disabling symmetry check, enabling/disabling optimization and alternate ways of synthesizing strategies
2. *tictactoe.properties*: In this file it is possible to choose between regular or simple rules, where simple rules require 2-in-a-row for winning
3. *sim.properties*: In this file it is possible to choose between regular and simple rules, where simple rules award win to first player to not get 6 lines in the hexagon.
4. *mens_morris.properties*: In this file, you can choose between playing Six Men's Morris and Three Men's Morris.
5. *kulibrat.properties*: In this file the board size can be altered, and the name of the database is specified.
6. *ggp.properties*: The ggp game file path should be specified here.
