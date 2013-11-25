CC = g++
CFLAGS = -w -fpermissive
EXEC_NAME = server
INCLUDES =
LIBS = -pthread
OBJ_FILES = main.o server.o
INSTALL_DIR = ./../

all: $(EXEC_NAME)

$(EXEC_NAME): $(OBJ_FILES)
	$(CC) -o $(EXEC_NAME) $(OBJ_FILES) $(LIBS)

%.o: %.cpp
	$(CC) $(CFLAGS) $(INCLUDES) -o $@ -c $<

%.o: %.cc
	$(CC) $(CFLAGS) $(INCLUDES) -o $@ -c $<

%.o: %.c
	gcc $(CFLAGS) $(INCLUDES) -o $@ -c $<

install:
	cp $(EXEC_NAME) $(INSTALL_DIR)

.PHONY: clean

clean:
	rm $(EXEC_NAME) $(OBJ_FILES)
