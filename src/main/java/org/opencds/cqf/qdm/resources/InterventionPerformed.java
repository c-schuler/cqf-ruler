package org.opencds.cqf.qdm.resources;

import ca.uhn.fhir.model.api.annotation.Child;
import org.opencds.cqf.cql.runtime.Code;
import org.opencds.cqf.cql.runtime.DateTime;
import org.opencds.cqf.cql.runtime.Interval;
import org.opencds.cqf.qdm.QdmBaseType;

import java.util.List;

public abstract class InterventionPerformed extends QdmBaseType {

    @Child(name="authorDatetime", order=0)
    DateTime authorDatetime;
    public DateTime getAuthorDatetime() {
        return authorDatetime;
    }
    public InterventionPerformed setAuthorDatetime(DateTime authorDatetime) {
        this.authorDatetime = authorDatetime;
        return this;
    }
	
	
    @Child(name="relevantPeriod", order=1)
    Interval relevantPeriod;
    public Interval getRelevantPeriod() {
        return relevantPeriod;
    }
    public InterventionPerformed setRelevantPeriod(Interval relevantPeriod) {
        this.relevantPeriod = relevantPeriod;
        return this;
    }	

	
    @Child(name="reason", order=2)
    Code reason;
    public Code getReason() {
        return reason;
    }
    public InterventionPerformed setReason(Code reason) {
        this.reason = reason;
        return this;
    }	

	
    @Child(name="status", order=3)
    Code status;
    public Code getStatus() {
        return status;
    }
    public InterventionPerformed setStatus(Code status) {
        this.status = status;
        return this;
    }	
	
	
    @Child(name="negationRationale", order=4)
    private Code negationRationale;
    public Code getNegationRationale() {
        return negationRationale;
    }
    public InterventionPerformed setNegationRationale(Code negationRationale) {
        this.negationRationale = negationRationale;
        return this;
    }
	
	

	
}
