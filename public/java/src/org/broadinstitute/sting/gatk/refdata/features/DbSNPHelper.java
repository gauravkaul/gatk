/*
 * Copyright (c) 2011, The Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.broadinstitute.sting.gatk.refdata.features;

import net.sf.samtools.util.SequenceUtil;
import org.broad.tribble.annotation.Strand;
import org.broad.tribble.dbsnp.DbSNPFeature;
import org.broadinstitute.sting.utils.Utils;
import org.broadinstitute.sting.utils.variantcontext.VariantContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * this class contains static helper methods for DbSNP
 */
public class DbSNPHelper {

    private DbSNPHelper() {} // don't make a DbSNPHelper

    public static String rsIDOfFirstRealVariant(List<VariantContext> VCs, VariantContext.Type type) {
        if ( VCs == null )
            return null;

        String rsID = null;
        for ( VariantContext vc : VCs ) {
            if ( vc.getType() == type ) {
                rsID = vc.getID();
                break;
            }
        }

        return rsID;
    }

    /**
     * get the -1 * (log 10 of the error value)
     *
     * @return the log based error estimate
     */
    public static double getNegLog10PError(DbSNPFeature feature) {
        return 4; // -log10(0.0001)
    }

    //
    // What kind of variant are we?
    //
    // ----------------------------------------------------------------------
    public static boolean isSNP(DbSNPFeature feature) {
        return feature.getVariantType().contains("single") && feature.getLocationType().contains("exact");
    }

    public static boolean isMNP(DbSNPFeature feature) {
        return feature.getVariantType().contains("mnp") && feature.getLocationType().contains("range");
    }

    public static String toMediumString(DbSNPFeature feature) {
        String s = String.format("%s:%d:%s:%s", feature.getChr(), feature.getStart(), feature.getRsID(), Utils.join("",feature.getObserved()));
        if (isSNP(feature)) s += ":SNP";
        if (isIndel(feature)) s += ":Indel";
        if (isHapmap(feature)) s += ":Hapmap";
        if (is2Hit2Allele(feature)) s += ":2Hit";
        return s;
    }

    public static boolean isInsertion(DbSNPFeature feature) {
        return feature.getVariantType().contains("insertion");
    }

    public static boolean isDeletion(DbSNPFeature feature) {
        return feature.getVariantType().contains("deletion");
    }

    public static boolean isIndel(DbSNPFeature feature) {
        return DbSNPHelper.isInsertion(feature) || DbSNPHelper.isDeletion(feature) || DbSNPHelper.isComplexIndel(feature);
    }

    public static boolean isComplexIndel(DbSNPFeature feature) {
        return feature.getVariantType().contains("in-del");
    }

    public static boolean isHapmap(DbSNPFeature feature) {
        return feature.getValidationStatus().contains("by-hapmap");
    }

    public static boolean is2Hit2Allele(DbSNPFeature feature) {
        return feature.getValidationStatus().contains("by-2hit-2allele");
    }

    public static boolean is1000genomes(DbSNPFeature feature) {
        return feature.getValidationStatus().contains("by-1000genomes");
    }

    public static boolean isMQ1(DbSNPFeature feature) {
        return feature.getWeight() == 1;
    }

    /**
     * gets the alternate alleles.  This method should return all the alleles present at the location,
     * NOT including the reference base.  This is returned as a string list with no guarantee ordering
     * of alleles (i.e. the first alternate allele is not always going to be the allele with the greatest
     * frequency).
     *
     * @return an alternate allele list
     */
    public static List<String> getAlternateAlleleList(DbSNPFeature feature) {
        List<String> ret = new ArrayList<String>();
        for (String allele : getAlleleList(feature))
            if (!allele.equals(String.valueOf(feature.getNCBIRefBase()))) ret.add(allele);
        return ret;
    }

    public static boolean onFwdStrand(DbSNPFeature feature) {
        return feature.getStrand() == Strand.POSITIVE;
    }

    public static String getReference(DbSNPFeature feature) {
        return feature.getNCBIRefBase();
    }

    public static String toSimpleString(DbSNPFeature feature) {
        return String.format("%s:%s:%s", feature.getRsID(), feature.getObserved(), (feature.getStrand() == Strand.POSITIVE) ? "+" : "-");
    }

    /**
     * gets the alleles.  This method should return all the alleles present at the location,
     * including the reference base.  The first allele should always be the reference allele, followed
     * by an unordered list of alternate alleles.
     *
     * @return an alternate allele list
     */
    public static List<String> getAlleleList(DbSNPFeature feature) {
        List<String> alleleList = new ArrayList<String>();
            // add ref first
            if ( onFwdStrand(feature) )
                alleleList = Arrays.asList(feature.getObserved());
            else
                for (String str : feature.getObserved())
                    alleleList.add(SequenceUtil.reverseComplement(str));
            if ( alleleList.size() > 0 && alleleList.contains(getReference(feature)) && !alleleList.get(0).equals(getReference(feature)) )
                Collections.swap(alleleList, alleleList.indexOf(getReference(feature)), 0);

        return alleleList;
    }
}
