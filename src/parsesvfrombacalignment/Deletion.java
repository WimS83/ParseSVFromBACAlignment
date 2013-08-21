/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parsesvfrombacalignment;

import org.apache.commons.lang3.Range;

/**
 *
 * @author wim
 */
public class Deletion {
    
    Range<Integer> location;
    String chromosome;

    public Deletion(Range<Integer> location, String chromosome) {
        this.location = location;
        this.chromosome = chromosome;
    }
    
    public void updateLocationEnd(Integer locationEnd)
    {
        location = Range.between(location.getMinimum(), locationEnd);    
    }  
    

    public Range<Integer> getLocation() {
        return location;
    }

    public void setLocation(Range<Integer> location) {
        this.location = location;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }
    
    
    
    
    
    
}
