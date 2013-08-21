/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parsesvfrombacalignment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceRecord;
import org.apache.commons.lang3.Range;

/**
 *
 * @author wim
 */
public class ParseSVFromBACAlignment {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
       // File inputBam = new File("/home/wim/Analysis/ratfounder/bac/rnor3.4/13BACSOnlyHQSorted.bam"); 
        
        File inputBam = new File("/home/wim/Analysis/ratfounder/bac/rnor5/13BACS_OnlyHighQualSorted.bam"); 
        
        parseIndelInAlignments(inputBam);
        
        parseIndelsBetweenAlignments(inputBam);
        
        
      
        
        
        
        // TODO code application logic here
    }

    private static void parseIndelInAlignments(File inputBam) {
        SAMFileReader bamreader = new SAMFileReader(inputBam);
        
        List<Deletion> deletionList = new ArrayList<Deletion>();
        
        
        
        
        
        for(SAMRecord samRecord : bamreader)
        {
            //skip alignment with low mapping qual
            if(samRecord.getMappingQuality() < 60 ){continue;}
            
            //get the start of the alignment
            String currentChrom = samRecord.getReferenceName();
            Integer currentReferencePos = samRecord.getAlignmentStart();
            
            for (CigarElement cigarElement :samRecord.getCigar().getCigarElements())
            {
                if(cigarElement.getOperator() == CigarOperator.D)
                {
                    Integer cigarElementStart = currentReferencePos;
                    Integer cigarElementLenght = cigarElement.getLength();
                    Integer cigarElementEnd = currentReferencePos+cigarElementLenght;
                    
                    if(cigarElementLenght > 100 )
                    {
                        //System.out.println("deletion found from " + currentChrom +":"  + cigarElementStart + "-" + cigarElementEnd );
                        //System.out.println(currentChrom+ "\t"+cigarElementStart+"\t"+cigarElementEnd+"\t"+"deletion");
                        //if there are previous deletions check is this one falls within 100 bp of the previous one. If so update the of the last one to be the end of this one 
                        if(deletionList.size() > 0)
                        {
                            Deletion previousDeletion = deletionList.get(deletionList.size()-1 );
                            Integer previousDeletionEnd = previousDeletion.getLocation().getMaximum();
                            
                            
                            if(previousDeletion.getChromosome().equals(currentChrom) &&  cigarElementStart - previousDeletionEnd  < 1000   )
                            {
                                previousDeletion.updateLocationEnd(cigarElementEnd);
                            }
                            else
                            {
                                 Deletion deletion = new Deletion(Range.between(cigarElementStart, cigarElementEnd), currentChrom);
                                deletionList.add(deletion);
                            }
                        }
                        else
                        {
                             Deletion deletion = new Deletion(Range.between(cigarElementStart, cigarElementEnd), currentChrom);
                             deletionList.add(deletion);
                        }
                        
                        
                       
                        
                        
                    }  
                }
                 if(cigarElement.getOperator() == CigarOperator.I)
                {
                    Integer cigarElementStart = currentReferencePos;
                    Integer cigarElementLenght = cigarElement.getLength();
                   
                    
                    if(cigarElementLenght > 100 )
                    {
                        //System.out.println("insertion found at " + currentChrom +":"  + cigarElementStart );
                        //System.out.println(currentChrom+ "\t"+(cigarElementStart-1)+"\t"+cigarElementStart+"\t"+"insertion");
                    }  
                }  
                
                
                //add the lenght of this cigarElement to the current pos to go to the start of the next element
                if(cigarElement.getOperator().consumesReferenceBases())
                {
                     currentReferencePos = currentReferencePos + cigarElement.getLength();
                } 
            }            
        }
        
        for(Deletion deletion : deletionList)
        {
            System.out.println(deletion.getChromosome()+ "\t"+deletion.getLocation().getMinimum()+"\t"+deletion.getLocation().getMaximum()+"\t"+"deletion");
        }
        
        
    }

    private static void parseIndelsBetweenAlignments(File inputBam) {
        
        
        SAMFileReader bamreader = new SAMFileReader(inputBam);
        
        //put the alignments in a map entry per bac
        Map<String, ArrayList<SAMRecord>> mapContigAlignments = new HashMap<String, ArrayList<SAMRecord>>();        
        for(SAMRecord samRecord : bamreader)
        {
            String readName  = samRecord.getReadName();
            
            if(!mapContigAlignments.containsKey(readName))
            {
                 ArrayList<SAMRecord> alignmentList = new ArrayList<SAMRecord>();
                 mapContigAlignments.put(readName, alignmentList);
            }
            
            mapContigAlignments.get(readName).add(samRecord);          
            
            
            String blaat = "blaat";
            
        
        }
        
        
        for(String bac : mapContigAlignments.keySet())
        {
            ArrayList<SAMRecord> records = mapContigAlignments.get(bac);
            Integer numberOfAlignments = records.size();
            System.out.println();
            System.out.println(bac +"  has " + numberOfAlignments + " alignments" );
            
            for(SAMRecord sAMRecord : records)
            {
                String chromName = sAMRecord.getReferenceName();
                Integer start = sAMRecord.getAlignmentStart();
                Integer stop = sAMRecord.getAlignmentEnd();
                
                System.out.println(chromName+":"+start+"-"+stop);  
            }
            
            
            TreeMap<Integer, SAMRecord> mapStartAlignments = new TreeMap<Integer, SAMRecord>();   
            
            for(SAMRecord sAMRecord : mapContigAlignments.get(bac))
            {
                mapStartAlignments.put(sAMRecord.getAlignmentStart(), sAMRecord);                
            }
            
            SAMRecord previousRecord = null;
            
            for (Integer key : mapStartAlignments.keySet()) { 
                 
                 SAMRecord currentRecord = mapStartAlignments.get(key);
                 
                 if(previousRecord != null)
                 {
                     Integer previousRecordEnd = previousRecord.getAlignmentEnd();
                     Integer currentRecordStart = currentRecord.getAlignmentStart();
                     System.out.println("found deletion on chrom "+ previousRecord.getReferenceName()+ ":" + previousRecordEnd+"-"+currentRecordStart);
                 
                 }
                 
                 previousRecord = currentRecord;
                 
                 
                 
                 
            // do something
            }
            
            
        }
        
        
        
        
        
        String blaat = "blaat";
    }
}
