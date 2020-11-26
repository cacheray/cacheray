#!/usr/bin/python3
import unittest
import os.path as fs
import dwarf2json as d2w

files = [
        "test1.elf",
        "test2.elf",
        "test3.elf",
        "test4.elf",
        "test5.elf"
        ]
file_folder = "res/"
    

class TestDwarf2Json(unittest.TestCase):
    def setUp(self):
        self.data = d2w.init_data()

    def tearDown(self):
        del self.data

    def test_integrity_basic(self):
        # Check integrity on empty data
        self.assertTrue(d2w.check_integrity(self.data))

    def test_integrity_on_file(self):
        for f in files:
            f_data = d2w.handle_elf(file_folder + f)
            self.assertTrue(d2w.check_integrity(f_data))
        
def check_tests_compiled():
    for f_name in files:
        if not fs.isfile(file_folder + f_name):
            print(f_name + ' is missing. Have you compiled the tests?')
            return False
    return True

if __name__ == '__main__':
    if not check_tests_compiled():
        quit()
    unittest.main()
