<<filename:c:app/code/local/[Prefix]/[Module]/Model:"SampleModel">>
<<var:n:module prefix => [Prefix]>>
<<var:c:module name => [Module]>>
<?php
//models will extend Mage_Core_Model_Abstract 
class [Prefix]_[Module]_Model_SampleModel extends Mage_Core_Model_Abstract {
	function getName() {
		//do some "heavy" logic here
		return 'Your module is called [Module]';
    }
}
?>