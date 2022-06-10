
#Retrive changes
cd IntraActive-SDK-UI/
currentBranch = $(git rev-parse --abbrev-ref HEAD)
changesMade="$(git diff $currentBranch --name-only && git ls-files --others --exclude-standard)"

#Retrive WP recommencation
echo "Bassed on your changes in $currentBranch"
echo "these may be the webparts you want to update: "
curl -X GET  http://localhost:8080/getRecommendation?files=$changesMade

#Get Dev's inputs
#echo "Please enter your commit message"
#read commitM
#echo "Which webpart do you wish to update?"
#read WP





#Push changes
#git add .
#git commmit -m"$commitM"
#git push

#Send changes and targeted webparts to Java server aplication
#curl -X POST -d "$changesMade ---$WP"  -H "Content-Type: text/plain" http://localhost:8080/addWP/

# Declare an array with 6 elements
#array0=(one two three four five six)
# Print first element
#echo $array0 # => "one"
# Print first element
#echo ${array0[0]} # => "one"
# Print all elements
#echo ${array0[@]} # => "one two three four five six"
# Print number of elements
#echo ${#array0[@]} # => "6"
# Print number of characters in third element
#echo ${#array0[2]} # => "5"
# Print 2 elements starting from forth
#echo ${array0[@]:3:2} # => "four five"
# Print all elements. Each of them on new line.
#for i in "${array0[@]}"; do
#   echo "$i"
#done

# We have the usual if structure:
# use `man test` for more info about conditionals
#if [ $Name != $USER ]
#then
#   echo "Your name isn't your username"
#else
#    echo "Your name is your username"
#fi

# Bash uses a `case` statement that works similarly to switch in Java and C++:
#case "$Variable" in
    # List patterns for the conditions you want to meet
#    0) echo "There is a zero.";;
#    1) echo "There is a one.";;
#    *) echo "It is not null.";;  # match everything
#esac