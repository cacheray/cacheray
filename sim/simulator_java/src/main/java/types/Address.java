package types;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Address implements Comparable<Address>{
    public static final Address NULL_ADDRESS = new Address(-1);

    private Long value;

    public Address(String inValue){
        value = Long.decode(inValue);
    }

    public Address(long inValue) {
        value = Long.valueOf(inValue);
    }

    public Address() {
        value = Long.MIN_VALUE;
    }

    public Address aligned(long alignment) {
        return new Address(value - (value % alignment));
    }

    @Override
    public boolean equals(Object a){
        return value.equals(((Address)a).getValue());
    }

    @Override
    public String toString() {
        return "0x" + getBigIntValue().toString(16);
    }

    public BigInteger getBigIntValue()
    {
        Long longValue = value;
        byte [] numberAsArray = new byte[8];
        for(int i = 0; i < 8; i++)
        {
            numberAsArray[7 - i] = (byte)((longValue >>> (i * 8)) & 0xFF);
        }
        return new BigInteger(1, numberAsArray);
    }

    public Long getValue(){
        return value;
    }

    public Address advance(long length){
        return new Address(value + length);
    }

    @Override
    public int compareTo(Address address) {
        return Long.signum(this.value - address.getValue());
    }

    public boolean lessThan(Address address) {
        return this.value < address.getValue();
    }

    public boolean containsAddress(Address address, long size) {
        boolean overflow = this.value > (this.value + (long)size);
        if(overflow)
            return (this.value <= address.value || (this.value+size) > address.value);
        return (this.value <= address.value && (this.value+size) > address.value);
    }

    public long getOffset(Address address) {
        // TODO: make sure this works for overflowed longs
        return address.getValue() - this.value;
    }

    public long mod(long val) {
        return this.value % val;
    }
/*
    public Address[] getHitAddresses(short size, int blockSize) {
        List<Address> addr = new ArrayList<>();
        addr.add(this.aligned(blockSize));
        Address tempAddr = this;
        while (true){
            tempAddr = tempAddr.advance(size);
            if (this.containsAddress(tempAddr, size)){
                addr.add();
            }
        }
    }*/
}
