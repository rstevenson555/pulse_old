java -classpath .:/apps/artadmin/jars/coloradoER.jar:/apps/artadmin/jars/logParser.jar:/apps/artadmin/jars/classes12_01.zip logParser.StandardQueryTools
java -classpath .:/apps/artadmin/jars/coloradoER.jar:/apps/artadmin/jars/logParser.jar:/apps/artadmin/jars/classes12_01.zip logParser.Tester
cat SubjectAllPages AllPagesDataOrdered0807.html> AllPagesData.html
rm AllPagesDataOrdered0807.html
cat Subject30SecondLoad 30SecondLoad0807.html> 30SecondLoad.html
rm 30SecondLoad0807.html
cat SubjectHourlyUsage HourlyUsage0807.html> HourlyUsage.html
rm HourlyUsage0807.html
cat SubjectMachineUtilization MachineUtilization0807.html > MachineUtilization.html
mail i97.orders@mail.bcop.com < AllPagesData.html
mail i97.orders@mail.bcop.com < 30SecondLoad.html
mail bob.stevenson@mail.bcop.com < HourlyUsage.html
mail bob.stevenson@mail.bcop.com < MachineUtilization.html
mail bryce.alcock@mail.bcop.com < HourlyUsage.html
mail bryce.alcock@mail.bcop.com < MachineUtilization.html

