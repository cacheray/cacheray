#!/usr/bin/env python3
import sys
import json

from elftools.elf.elffile import ELFFile

# Increase this if a change is made in the format
VERSION_NBR = 4
POINTER_SIZE = 8


def create_json_file(die_data, base_name):
    """Write data pack to JSON file

    Keyword arguments:
    die_data -- data pack to write
    base_name -- file name to write to
    """
    fp = open(base_name + ".json", 'w')
    print("Writing DWARF data to JSON file...", end='')
    fp.write(json.dumps(die_data, indent = 4, sort_keys=True))
    print(" DONE!")
    fp.close()


def check_integrity(die_data):
    """Check that the data pack is properly formatted

    Keyword arguments:
    die_data -- data pack to check
    """
    print_info('Running integrity checks...')

    # Check for no duplicates
    c = 0
    for t in die_data['types']:
        for t2 in die_data['types']:
            if t['id'] == t2['id']:
                c += 1
        if c != 1:
            return False
        c = 0

    c = 0
    # Check for correct mapping
    for mem in die_data['structs']:
        # Check that struct is in type list
        found = False
        for t in die_data['types']:
            if (t['id'] == mem['id']):
                found = True
        if not found:
            return False
        
        # Check that struct is unique
        for mem2 in die_data['structs']:
            if mem['id'] == mem2['id']:
                c += 1
        if c != 1:
            print_info('Count is ' + str(c))
            return False
        c = 0

    print_info('Integrity is fine!')
    return True


def init_data():
    """Create and initialize a data pack
    """
    data_pack = {"version": VERSION_NBR, "structs": [], "unions": [], "name_list": [], "typedefs": []}
    # Here we can initialize further variables and stuff
    # Temporarily store the connection between index and name for vars
    return data_pack


def my_hash(text):
    """Create unique hash from name.

    Used to keep track of type names in different contexts.

    Keyword arguments:
    text -- string to hash
    """
    return hash(text)


def type_exists(data, type_name):
    """Check if type exists in data pack

    Keyword arguments:
    data -- data structure where type might be found
    struct_name -- name of the type to search for
    
    """
    for established_type in data['types']:
        if established_type['name'] == type_name:
            return True
    return False


def exists_in_data(die_data, name, table):
    """Check if name exists in the DIE data

    Keyword arguments:
    die_data -- data structure where struct might be found
    name -- name of the type to search for
    table -- which table to search
    """
    for data_type in die_data[table]:
        if data_type is None:
            continue
        if data_type['name'] == name:
            return data_type
    return None


def struct_exists(die_data, struct_name):
    return exists_in_data(die_data, struct_name, 'structs')


def union_exists(die_data, union_name):
    return exists_in_data(die_data, union_name, 'unions')


def get_die_from_type_data(type_data, offset):
    return type_data[offset] if offset in type_data else None


def get_die_proper_name(type_data, die):
    if die is None:
        return "<unknown_name>"
    tag = die.tag
    if tag == 'DW_TAG_pointer_type':
        if 'DW_AT_type' in die.attributes:
            offset = die.attributes['DW_AT_type'].value + die.cu.cu_offset
            if offset not in type_data:
                return "<unknown pointer type>"
            return get_die_proper_name(type_data, type_data[offset]) + "*"
        else:
            # Void pointer
            return "void*"
    elif tag == 'DW_TAG_array_type':
        offset = die.attributes['DW_AT_type'].value + die.cu.cu_offset
        if offset not in type_data:
            return "<unknown type>"
        return get_die_proper_name(type_data, type_data[offset]) + "[]"
    elif tag == 'DW_TAG_base_type':
        return die.attributes['DW_AT_name'].value.decode('ascii')
    elif tag == 'DW_TAG_typedef':
        if 'DW_AT_type' not in die.attributes:
            # Typedef is void. This is very stupid
            return "void"
        offset = die.attributes['DW_AT_type'].value + die.cu.cu_offset
        if offset not in type_data:
            return "<unknown typedef>"
        return get_die_proper_name(type_data, type_data[offset])
    elif tag == 'DW_TAG_const_type':
        if 'DW_AT_type' not in die.attributes:
            # Typedef is void. This is very stupid
            return "const void"
        offset = die.attributes['DW_AT_type'].value + die.cu.cu_offset
        return "const " + get_die_proper_name(type_data, type_data[offset])
    elif tag == 'DW_TAG_structure_type' or tag == 'DW_TAG_union_type':
        return "struct " + (die.attributes['DW_AT_name'].value.decode('ascii') if 'DW_AT_name' in die.attributes else "<anon>")
    elif tag == 'DW_TAG_member':
        offset = die.attributes['DW_AT_type'].value + die.cu.cu_offset
        return get_die_proper_name(type_data, type_data[offset])
    else:
        #print("Type was " + tag)
        return "<unsupported type>"


def get_die_proper_size(type_data, die):
    if die is None:
        return -1
    if die.tag == 'DW_TAG_pointer_type':
        return POINTER_SIZE
    if die.tag == 'DW_TAG_array_type':
        offset = die.attributes['DW_AT_type'].value + die.cu.cu_offset
        if offset not in type_data:
            return -1
        actual_type = type_data[offset]
        subtype = list(die.iter_children())[0]
        if 'DW_AT_count' in subtype.attributes:
            return subtype.attributes['DW_AT_count'].value * get_die_proper_size(type_data, actual_type) #actual_type.attributes['DW_AT_byte_size'].value

    if 'DW_AT_type' in die.attributes:
        offset = die.attributes['DW_AT_type'].value + die.cu.cu_offset
        return get_die_proper_size(type_data, type_data[offset])

    return die.attributes['DW_AT_byte_size'].value if 'DW_AT_byte_size' in die.attributes else -1


def get_type_info(type_data, offset):
    die = get_die_from_type_data(type_data, offset)
    formatted_data = {'name': get_die_proper_name(type_data, die), 'size' : get_die_proper_size(type_data, die)}
    return formatted_data


def get_name_from_temp(data, index):
    """Get real name of tag from namelist

    Keyword arguments:
    data -- structure where list recides
    index -- index in list
    """
    return my_hash(data['temp_type_index'][index]) if index in data['temp_type_index'] else -1


def is_type_pointer(die):
    return die.tag == 'DW_TAG_pointer_type'


def make_type_table(die):
    #if die.is_null():
    #    return None
    type_table = {die.offset: die}
    for child_die in die.iter_children():
        type_table.update(make_type_table(child_die))
    return type_table


def collect(die_data, type_data, collection, _type="default"):
    # Step one: identify current package
    if _type != "default":
        if die_data.tag is not _type:
            print_info("Tried to collect (" + _type + ") from (" + die_data.tag + ")")
            raise TypeError


def collect_struct(die_data, struct, type_data):
    if struct.tag != 'DW_TAG_structure_type':
        # check that we actually have a structure type here
        print_info("Tried to collect struct from non struct member")
        raise TypeError
    struct_copy = {}
    struct_name = "<anon_struct>"
    if 'DW_AT_name' in struct.attributes:
        struct_name = "struct " + struct.attributes['DW_AT_name'].value.decode('ascii')
    established_struct = struct_exists(die_data, struct_name)
    if established_struct is not None:
        # struct already exists. exit
        return established_struct
    struct_copy['id'] = struct.offset
    struct_copy['name'] = struct_name
    struct_members = []
    struct_member_list = list(struct.iter_children())
    for index in range(len(struct_member_list)):
        struct_child = struct_member_list[index]
        tag = struct_child.tag
        if tag != 'DW_TAG_member':
            continue

        if 'DW_AT_name' not in struct_child.attributes:
            # Is probably anonymous struct or union

            # Check nest node, might be the correct type
            struct_child_offset = struct_child.attributes['DW_AT_type'].value + struct_child.cu.cu_offset
            if struct_child_offset not in type_data:
                # Maybe look at next?
                if index+1 >= len(struct_member_list):
                    # idk wtf to do here
                    return None
                struct_child_def = struct_member_list[index+1]
            else:
                struct_child_def = type_data[struct_child_offset]
            offset = struct_child.attributes['DW_AT_data_member_location'].value
            member = {}
            if struct_child_def.tag == 'DW_TAG_structure_type':
                underlying_struct = collect_struct(die_data, struct_child_def, type_data)
                member['id'] = underlying_struct['id']
                member['name'] = underlying_struct['name']
                member['size'] = struct_child_def.attributes['DW_AT_byte_size'].value
                member['memberName'] = underlying_struct['name']
                member['offset'] = offset
            elif struct_child_def.tag == 'DW_TAG_union_type':
                underlying_union = collect_union(die_data, struct_child_def, type_data)
                member['id'] = underlying_union['id']
                member['name'] = underlying_union['name']
                member['size'] = struct_child_def.attributes['DW_AT_byte_size'].value
                member['memberName'] = underlying_union['name']
                member['offset'] = offset
            struct_members.append(member)

        else:
            m_name = struct_child.attributes['DW_AT_name'].value.decode('ascii')
            index = struct_child.attributes['DW_AT_type'].value + struct_child.cu.cu_offset
            offset = struct_child.attributes['DW_AT_data_member_location'].value
            type_die = get_die_from_type_data(type_data, index)
            name = get_die_proper_name(type_data, struct_child)
            member = {'id': type_die.offset, 'size': get_die_proper_size(type_data, type_die), 'name' : name, 'memberName': m_name,
                      'offset': offset}
            struct_members.append(member)
    struct_copy['members'] = struct_members
    return struct_copy


def collect_union(die_data, union, type_data):
    if union.tag != 'DW_TAG_union_type':
        print_info("Tried to collect union from non union member")
        raise TypeError
    union_copy = {}
    union_name = "<anon_union>"
    if 'DW_AT_name' in union.attributes:
        union_name = union.attributes['DW_AT_name'].value.decode('ascii')
    established_union = union_exists(die_data, union_name)
    if established_union is not None:
        return established_union
    union_copy['id'] = union.offset
    union_copy['name'] = union_name
    union_members = []
    union_member_list = list(union.iter_children())
    for index in range(len(union_member_list)):
        union_child = union_member_list[index]
        tag = union_child.tag
        if tag != 'DW_TAG_member':
            continue
        if 'DW_AT_name' not in union_child.attributes:
            # Is probably anonymous struct or union
            next_union_child = union_member_list[index + 1]
            member = {}
            if next_union_child.tag == 'DW_TAG_structure_type':
                underlying_struct = collect_struct(die_data, next_union_child, type_data)
                member = {'id': underlying_struct['id'], 'size': next_union_child.attributes['DW_AT_byte_size'].value, 'name' : underlying_struct['name'], 'memberName': underlying_struct['name']}
            elif next_union_child.tag == 'DW_TAG_union_type':
                underlying_union = collect_union(die_data, next_union_child, type_data)
                member = {'id': underlying_union['id'], 'size': next_union_child.attributes['DW_AT_byte_size'].value, 'name' : underlying_union['name'], 'memberName': underlying_union['name']}
            union_members.append(member)

        else:
            member = {}
            m_name = union_child.attributes['DW_AT_name'].value.decode('ascii')
            index = union_child.attributes['DW_AT_type'].value + union_child.cu.cu_offset
            type_die = get_die_from_type_data(type_data, index)
            member['id'] = type_die.offset
            member['name'] = get_die_proper_name(type_data, union_child)
            member['size'] = get_die_proper_size(type_data, type_die)
            member['memberName'] = m_name
            union_members.append(member)
    union_copy['members'] = union_members
    return union_copy


def iterate_collect(die_data, die, collect_function):
    """Collect the tags in the right order

    Keyword arguments:
    data -- object to store the tags in
    die -- the root of the DIE tree
    collect_function -- function used for collect....
    """
    if die.tag == 'DW_TAG_compile_unit':
        for cu_child in die.iter_children():
            collect_function(die_data, cu_child)
    else:
        for cu_child in die.iter_children():
            iterate_collect(die_data, cu_child, collect_function)


def collect_structs_and_unions(die_data, die, type_data):
    if die.tag == 'DW_TAG_compile_unit':
        for cu_child in die.iter_children():
            if cu_child.tag == 'DW_TAG_structure_type':
                struct = collect_struct(die_data, cu_child, type_data)
                if struct['name'] not in die_data['name_list']:
                    die_data['structs'].append(struct)
                    die_data['name_list'].append(struct['name'])
            elif cu_child.tag == 'DW_TAG_union_type':
                union = collect_union(die_data, cu_child, type_data)
                if union['name'] not in die_data['name_list']:
                    die_data['unions'].append(union)
                    die_data['name_list'].append(union['name'])
            elif cu_child.tag == 'DW_TAG_typedef':
                name = cu_child.attributes['DW_AT_name'].value.decode('ascii')
                offset = cu_child.attributes['DW_AT_type'].value + cu_child.cu.cu_offset
                actual = get_die_proper_name(type_data, type_data[offset])
                typedef = {"type": name, "def": actual}
                die_data['typedefs'].append(typedef)
            else:
                continue
    else:
        for cu_child in die.iter_children():
            collect_structs_and_unions(die_data, cu_child, type_data)


def check_type_data_closure(type_data):
    n_die = len(type_data)
    n_hits = 0
    n_misses = 0
    for index in list(type_data):
        die = type_data[index]
        if 'DW_AT_type' in die.attributes:
            if die.attributes['DW_AT_type'].value + die.cu.cu_offset not in type_data:
                n_misses += 1
            else:
                n_hits += 1
    print("N_DIE: " + str(n_die) + "\nN_HITS: " + str(n_hits) + "\nN_MISSES: " + str(n_misses))


def handle_elf(file_name):
    """Generate JSON from file

    Keyword arguments:
    filename -- path of the ELF file to extract
    """
    with open(file_name, 'rb') as f:
        print("Loading ELF file...")
        elf_file = ELFFile(f)

        if not elf_file.has_dwarf_info():
            print_info("File " + file_name + " has no dwarf info")
            return

        print("Extracting DWARF data...")
        dwarf_info = elf_file.get_dwarf_info()

        print_info("Initializing data...")
        dwarf_data = init_data()
        type_data_pack = {}
        print_info("Creating type look-up table...")
        dwarf_info_cus = list(dwarf_info.iter_CUs())
        amount_cus = len(dwarf_info_cus)
        index = 1
        for CU in dwarf_info_cus:
            print("Handling CUs " + str(index) + "/" + str(amount_cus) + "      \r", end='')
            top_die = CU.get_top_DIE()
            type_data_pack.update(make_type_table(top_die))
            index += 1
        print("\nDone creating type look-up table!")
        print("Checking type info correctness...")
        check_type_data_closure(type_data_pack)
        print("Done checking type info correctness!")
        print("Generating type info...")
        index = 1
        for CU in dwarf_info_cus:
            top_die = CU.get_top_DIE()
            cu_name = top_die.get_full_path().split('/')[-1]
            print("Handling CUs " + str(index) + "/" + str(amount_cus) + "       \r", end='')
            top_die = CU.get_top_DIE()
            # iterate_collect(dwarf_data, top_die, collect_structs)
            collect_structs_and_unions(dwarf_data, top_die, type_data_pack)
            index += 1
        print("\nDone generating type info!")
        del dwarf_data['name_list']
        return dwarf_data


def print_info(*args):
    """Print info if dwarf2json is run on its own

    Used to suppress output during testing.

    Keyword arguments:
    *args -- variadic arglist, same that print uses basically
    """
    if __name__ == '__main__':
        print("".join(map(str, args)))


if __name__ == '__main__':
    if len(sys.argv) > 1:
        # Extract all files in arglist
        for filename in sys.argv[1:]:
            data = handle_elf(filename)
            create_json_file(data, filename.split('.')[0] + '.dwarf')
