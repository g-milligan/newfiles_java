<<filename:c:app/code/[codepool]/[Prefix]/[Module]/Model/System/Config/Source:"Example">>
<<var:n:module prefix => [Prefix]>>
<<var:c:module name => [Module]>>
<<var:l:module prefix => [prefix]>>
<<var:l:module name => [module]>>
<<var:l:code pool => [codepool]>>

<?php
class [Prefix]_[Module]_Model_System_Config_Source_Example
{

    public function toOptionArray()
    {
        return array(
            array('value' => 0, 'label'=>Mage::helper('adminhtml')->__('Custom Option 1')),
            array('value' => 1, 'label'=>Mage::helper('adminhtml')->__('Custom Option 2')),
            array('value' => 2, 'label'=>Mage::helper('adminhtml')->__('Custom Option 3')),
            array('value' => 3, 'label'=>Mage::helper('adminhtml')->__('Custom Option 4')),
        );
    }

}