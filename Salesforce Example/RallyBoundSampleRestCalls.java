public class RallyBoundSampleRestCalls {

	public static String PostOfflineDonation(){
	
        decimal donationAmount = 100;
        Integer fundraiserGoalId = 123;
        long unixTimestamp = 1333324800;
        
        String jsonString = '{' + 
          '"Donation": {' +
            '"Amount": ' + donationAmount + ',' + 
            '"Message": "test msg",' + 
            '"Donor": {' + 
              '"Id": null,' + //pass UserId if known, in which case you would ommit name and email fields.
              '"First_Name": "first name",' + 
              '"Last_Name": "test last",' + 
              '"Email": "test-donor@a.com",' + 
              '"Addresses": [{' + 
                '"Address_1": "123 Main St",' + 
                '"Address_2":  null,' + 
                '"City": "LA",' + 
                '"State": "CA",' + 
                '"Zip": "90001",' + 
                '"Country": "US"' + 
              '}]' +
            '},' + 
            '"Recipient": {' + 
				'"Id": ' + fundraiserGoalId + //alternatively pass User_Id or Team_Id, not passing any Recipient will result in General Donation
			'},' +  
            '"Payment": {' + 
              '"Paid_At": ' + unixTimestamp + ',' +
              '"Currency_Key": "USD",' + 
              '"Payment_Type": "Cash",' + 
              '"Is_Verified": true' + 
            '},' + 
            '"Hide_Name": false,' + 
            '"Hide_Amount": false,' + 
            '"Is_Corp_Sponsorship": false' + 
          '}' +
        '}';
		
        RallyBoundRestClient request = new RallyBoundRestClient(
            'v2/admin/campaigns/{{Campaign.Id}}/donations',
            'POST',
            jsonString
        );
		System.debug(request.responseBody);
		
        return request.responseBody;
    }
}