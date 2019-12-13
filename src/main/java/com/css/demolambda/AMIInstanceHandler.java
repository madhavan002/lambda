package com.css.demolambda;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;


/**
 * @author madhavan
 *
 */
public class AMIInstanceHandler implements RequestHandler<AMIInstanceRequest, AMIInstanceResponse>{

	
	/**
	 *Lambda handler function
	 *@param request - Two string type inputs are passed - EC2 instance tag name nad value.
     *@param context  
     *@return response - AMIInstanceResponse - Returns the matching instance with given tag name and value.
	 */
	public AMIInstanceResponse handleRequest(AMIInstanceRequest request, Context context) {
		System.out.println("Going to check the instances with Tag Key["+request.getTagKey()+"] Value["+request.getTagValue()+"]");
		AMIInstanceResponse response = new AMIInstanceResponse();
		List<String> instanceIDList = new ArrayList<String>();
		if(null == request.getTagKey() || 
		   null == request.getTagValue() ||
		   "".equals(request.getTagKey().trim()) || 
		   "".equals(request.getTagValue().trim())
		  ) {
			System.out.println("Either supplied tag key or value is empty or null. So don't process further. Return an empty instance list.");
		}else {
			final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
			boolean done = false;
	        DescribeInstancesRequest instRequest = new DescribeInstancesRequest();
	        while(!done) {
	            DescribeInstancesResult dResponse = ec2.describeInstances(instRequest);
	
	            for(Reservation reservation : dResponse.getReservations()) {
	                for(Instance instance : reservation.getInstances()) {
	                    System.out.printf(
	                        "Found instance with id %s, " +
	                        "AMI %s, " +
	                        "type %s, " +
	                        "state %s " +
	                        "and monitoring state %s",
	                        instance.getInstanceId(),
	                        instance.getImageId(),
	                        instance.getInstanceType(),
	                        instance.getState().getName(),
	                        instance.getMonitoring().getState());
	                    	//Loop through all the tags for the instance
		                    for (Tag tag : instance.getTags()) {
		                    	//Instance tag key is matching the given tag key?
		                        if (tag != null && tag.getKey() != null
		                                && tag.getKey().equals(request.getTagKey())) {
		                        	//Instance tag value is matching the given tag value??
		                        	//Then add this instance to the return list
		                            if(tag.getValue().equals(request.getTagValue())){
		                            	System.out.printf(
		             	                        "Found a matching instance with id %s ",
		             	                        instance.getInstanceId()
		             	                        );
		                            	instanceIDList.add(instance.getInstanceId());
		                            }
		                        }
		                    }
	                }
	                
	            }
	
	            instRequest.setNextToken(dResponse.getNextToken());
	
	            if(dResponse.getNextToken() == null) {
	                done = true;
	            }
	        }
		}
        response.setInstanceIDList(instanceIDList);
        return response;
	}
	
}
