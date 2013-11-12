CC = gcc
CFLAGS=-W -Wall -Wextra

EXEC=client server
SRC= $(wildcard *.c)
OBJ= $(SRC:.c=.o)

all: $(EXEC)

$(EXEC):
	$(CC) $@.c -o ../bin/$@ $(CFLAGS)

.PHONY: clean

clean:
	rm -rf ../bin/*
