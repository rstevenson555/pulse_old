java -classpath .:/apps/artadmin/jars/coloradoER.jar:/apps/artadmin/jars/logParser.jar:/apps/artadmin/jars/classes12_01.zip logParser.Tester 0803
cat SubjectAllPages AllPagesData0806.html> AllPagesData.html
cat SubjectAllPagesO AllPagesDataOrdered0806.html> AllPagesDataOrdered.html
cat Subject30SecondLoad 30SecondLoad0806.html> 30SecondLoadOrdered.html
#mail I97.Orders@mail.bcop.com < 30SecondLoadOrdered.html
#mail I97.Orders@mail.bcop.com < AllPagesDataOrdered.html
mail bryce.alcock@mail.bcop.com < 30SecondLoadOrdered.html
mail bryce.alcock@mail.bcop.com < AllPagesDataOrdered.html
mail bryce.alcock@mail.bcop.com < HourlyUsage0806.html

