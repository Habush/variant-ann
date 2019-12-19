package org.mozi.varann.data.records;

import dev.morphia.annotations.Embedded;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embedded
public class GeneInfo {
    private String symbol;
    private String id;

    @Override
    public String toString(){
        return "GeneInfo[symbol=" + symbol + ", id=" + id +  "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        GeneInfo other = (GeneInfo)obj;
        if(symbol == null){
            if(other.symbol != null){
                return false;
            }
        }else if(!symbol.equals(other.symbol))
            return false;
        if(id == null){
            if(other.id != null){
                return false;
            }
        }else if(!id.equals(other.id))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }
}
