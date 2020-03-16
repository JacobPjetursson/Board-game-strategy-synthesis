## GLOBAL CONFIG
FILE=global.properties
if [ ! -f "$FILE" ]; then
echo "# Guide to global configurations!
# 1) *enable_autogen* should be set to *true* if you want to autogenerate an FFT on program startup

# 2) *autogen_perspective* chooses the player to autogenerate the winning strategy for
#    Accepted values are *player1*, *player2* and *both*

# 3) *greedy_autogen* decides the method for autogenerating the winning strategy
#    Set it to *false* for a better result that takes longer

# 4) *enable_ggp* decides whether General Game Playing data structures should be used after loading in existing
#    strategies. It does NOT decide whether to generate strategies using GGP or not

# 5) *enable_ggp_parser* decides whether a General Game Playing parser (which follows the GGP conventions)
#    should be used when saving and loading the FFT
#    You may want to autogenerate without using the GGP data structures but still save it in the GGP format

# 6) *random_rule_ordering* decides whether the ordering of generated rules in the FFT should be randomized
#    By default the ordering is based on the depth of the states, i.e. states close to terminal states in the
#    search tree is prioritized

# 7) *minimize_preconditions* decides whether the preconditions within rules should be minimized when minimizing the FFT
#    The main reason for setting to false would be for debugging purposes


autogen_perspective = player1
greedy_autogen = true
minimize_preconditions = true
random_rule_ordering = false

random_seed =true
seed = 0
# Below two properties are automatically set to true when executing the GGP program
# Generally, don't be concerned about these two properties, they rarely need to be altered
enable_ggp = false
enable_ggp_parser = false

# For debugging and testing
# 1) *detailedDebug* results in a lot more debug messages to the console when synthesizing the rules
# 2) *fullRules* determines if rules should be compressed or not. It can be set to *true* for debugging purposes
# 3) *verify_single_strategy* tries a different approach for generating the strategy. Currently unused
detailedDebug = false
single_thread = false
fullRules = false
verify_single_strategy = false
" > $FILE
fi

## GGP PROPERTIES
FILE=ggp.properties
if [ ! -f "$FILE" ]; then
echo "# Games can be found in src/main/java/fftlib/GGPAutogen/games
# We can recommend trying out *tictactoe.kif*, *tictactoe_simple.kif* and *nim.kif*
ggp_game =  tictactoe.kif
" > $FILE
fi

## TIC TAC TOE PROPERTIES
FILE=tictactoe.properties
if [ ! -f "$FILE" ]; then
echo "# Simple rules means the winner is the first player with 2-in-a-row
simple_rules = false
" > $FILE
fi

## KULIBRAT PROPERTIES
FILE=kulibrat.properties
if [ ! -f "$FILE" ]; then
echo "boardHeight = 3
boardWidth = 3
db_path = jdbc:derby:KulibratDB;create=true
" > $FILE
fi

## SIM PROPERTIES
FILE=sim.properties
if [ ! -f "$FILE" ]; then
echo "simple_rules = false
" > $FILE
fi