<<filename:c:app/code/local/[Prefix]/[Module]/Helper:"SampleHelper">>
<<var:n:module prefix => [Prefix]>>
<<var:c:module name => [Module]>>
<?php
//helpers will extend Mage_Core_Helper_Abstract 
class [Prefix]_[Module]_Helper_SampleHelper extends Mage_Core_Helper_Abstract{
	function shouldSayHi() {
		return true;
    }
}
?>