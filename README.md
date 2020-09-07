# Synthesizing human-friendly optimal strategies in board games

This project is the product of the 2020 Conference on Games paper "Synthesizing human-friendly optimal strategies in board games", written by me and Thomas Bolander.

It is also the product of my thesis from the Technical University of Denmark 2020.

## Project Setup:

1. Install Java8 or newer
2. Install [Maven 3](https://maven.apache.org/download.cgi)
3. Open the Terminal and navigate to project root
4. Run `mvn verify`
   - Check that build was run successfully and the */target* folder is created in the project root

## Project Execution

Project consists of 4 different programs:
- Tic-tac-toe
- Sim
- Three Men's Morris and Six Men's Morris
- General Game Playing (GGP)

Tic-tac-toe offers a GUI where it is possible to play around and test various strategies with FFT's

These programs can be run with the following commands:
1. `mvn exec:java@tictactoe` *or simply* `mvn exec:java`
2  `mvn exec:java@sim` 
3  `mvn exec:java@morris`
4. `mvn exec:java@ggp`

## Configuration

There are 6 configuration files in total that can alter the way the strategy is generated. There is a file for each type of program plus a global config file, which are called:
1. *global.properties*: This is the main configuration file which contains options such as disabling symmetry detection, enabling/disabling various optimization flags, enabling/disabling GUI and alternative ways of synthesizing strategies
2. *tictactoe.properties*: In this file it is possible to choose between regular or simple rules, where simple rules require 2-in-a-row for winning
3. *sim.properties*: In this file it is possible to choose between regular and simple rules, where simple rules award win to first player to not get 6 lines in the hexagon.
4. *mens_morris.properties*: In this file, you can choose between playing Six Men's Morris and Three Men's Morris.
5. *ggp.properties*: The ggp game file path is specified here
