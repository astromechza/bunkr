package com.bunkr_beta.interfaces;

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-12-02
 */
public interface ICLICommand
{
    void buildParser(Subparser target);
    void handle(Namespace args) throws IOException;
}
