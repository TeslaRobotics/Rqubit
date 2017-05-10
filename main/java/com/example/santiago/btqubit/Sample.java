package com.example.santiago.btqubit;

public class Sample {

    private String _balltimings;
    private String _fallposition;
    private String _rotortimings;

    public Sample() {
            super();
            _balltimings = "blank";
            _rotortimings = "blank";
            _fallposition = "blank";
    }

    public Sample(String s, String s1, String s2) {
            super();
            _balltimings = s;
            _rotortimings = s1;
            _fallposition = s2;
    }

    public String get_balltimings() {
            return _balltimings;
    }

    public String get_fallposition() {
            return _fallposition;
    }

    public String get_rotortimings() {
            return _rotortimings;
    }

    public void set_balltimings(String s) {
            _balltimings = s;
    }

    public void set_fallposition(String s) {
            _fallposition = s;
    }

    public void set_rotortimings(String s) {
            _rotortimings = s;
    }


}
