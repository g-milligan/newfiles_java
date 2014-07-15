<<filename:c:app/code/[codepool]/[Prefix]/[Module]/Model:"SampleModel">>
<<var:n:module prefix => [Prefix]>>
<<var:l:module prefix => [prefix]>>
<<var:c:module name => [Module]>>
<<var:l:module name => [module]>>
<<var:l:code pool => [codepool]>>

<?php
//models will extend Mage_Core_Model_Abstract 
class [Prefix]_[Module]_Model_SampleModel extends Mage_Core_Model_Abstract {
	function getName() {
		//do some "heavy" logic here
		return 'Your module is called [Module]';
    }
    
    function getConfigSettings () {
            
        // some examples of how to grab stuff from the module config page. 
        // fields are setup in app/code/[codepool]/[Prefix]/[Module]/etc/system.xml
        // can be found here System >> Configuration >> [Module] Settings
        
        // grab a specific setting or value
        // $specific_setting=Mage::getStoreConfig('[module]/option_group/textfield_input',Mage::app()->getStore());
        
        // Grab all configuration settings from the fields we setup in system.xml, return an array of values
        return $settings=Mage::getStoreConfig('[module]',Mage::app()->getStore());
                        
    }
}
