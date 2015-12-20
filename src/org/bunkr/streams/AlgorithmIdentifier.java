package org.bunkr.streams;

/**
 * Creator: benmeier
 * Created At: 2015-12-19
 */
public class AlgorithmIdentifier
{
    public enum Engine
    {
        AES256
    }

    public enum Algorithm
    {
        CTR
    }

    public enum Padding
    {
        PKCS7
    }

    public enum Compressor
    {
        DEFLATE
    }

    private final Engine engine;
    private final Algorithm algorithm;
    private final Padding padding;
    private final Compressor compressor;

    public AlgorithmIdentifier(Engine e, Algorithm a, Padding p, Compressor c)
    {
        this.engine = e;
        this.algorithm = a;
        this.padding = p;
        this.compressor = c;
    }

    public AlgorithmIdentifier(String s)
    {
        String[] parts = s.toUpperCase().split(",", -1);
        if (parts.length != 4) throw new IllegalArgumentException("Must contain 4 parts");
        this.engine = (parts[0].equals("")) ? null : Engine.valueOf(parts[0]);
        this.algorithm = (parts[1].equals("")) ? null : Algorithm.valueOf(parts[1]);
        this.padding = (parts[2].equals("")) ? null : Padding.valueOf(parts[2]);
        this.compressor = (parts[3].equals("")) ? null : Compressor.valueOf(parts[3]);
    }

    public String toString()
    {
        String o = "";
        if (this.engine != null) o += this.engine.name();
        o += ",";
        if (this.algorithm != null) o += this.algorithm.name();
        o += ",";
        if (this.padding != null) o += this.padding.name();
        o += ",";
        if (this.compressor != null) o += this.compressor.name();
        return o;
    }

    public Algorithm getAlgorithm()
    {
        return algorithm;
    }

    public Compressor getCompressor()
    {
        return compressor;
    }

    public Engine getEngine()
    {
        return engine;
    }

    public Padding getPadding()
    {
        return padding;
    }
}
