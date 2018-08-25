SET exceptionDisplaying=true
SET messageSaving=true
SET savingInterval=1000
java -cp ".;libaries/gson-2.6.2.jar" lifeTalk.server.Server %exceptionDisplaying% %messageSaving% %savingInterval%