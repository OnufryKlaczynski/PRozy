mpirun -np 4 \
  --hostfile /home/toor/PRoz/src/hostfile \
  --prefix /usr/local \
  /usr/bin/java \
  -Djava.library.path=/usr/local/lib \
  -Dfile.encoding=UTF-8 \
  -classpath /home/toor/PRoz/target/classes:/usr/local/lib/mpi.jar \
  proz.Main
