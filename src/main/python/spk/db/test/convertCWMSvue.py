print "Starting"
import csv
import sys


file = sys.argv[1]
print "Opening: " + file
with open(sys.argv[1]) as csvfile
    reader = csv.reader(csvfile)
    for row in reader:
        print ",".join(row)