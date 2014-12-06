function sample_json_templates(){
	json={
		'dirs':[
			{
				'path':'Demos/ListTokenDemo',
				'ls':
				[
					{'name':'fileA.css'},
					{'name':'fileB.phtml','tokens':
						[
						 	{'type':'filename','casing':'l','path':'path/to/file','name':'.'},
							{'type':'var','casing':'u','name':'some var','alias':'[myalias]'},
							{'type':'list','name':'some list'}
						]
					},
					{'name':'fileC.php'},
					{'name':'fileD.js','tokens':
						[
						 	{'type':'filename','casing':'l','path':'path/to/file','name':'.'},
							{'type':'var','casing':'u','name':'some var','alias':'[myalias]'},
							{'type':'list','name':'some list'}
						]
					},
					{'name':'fileE.html'},
					{'name':'fileF.txt'}
				],
				'includes':
				[
					'test/*/allFiles',
					'[css]/stuff*yep/sub.css',
					'[css]/includethis.*'
				],
				'hidden':
				[
					'_file1.css','_file2.phtml','_file3.php'
				]
			},
			{'path':'Demos/TestInclude','ls':
				[
					{'name':'fileA.css'},
					{'name':'fileB.phtml'}
				],
				'includes':
				[
					'test/*/allFiles'
				],
				'hidden':
				[
					'_file1.css'
				]
			},
			{'path':'Demos/TestVarOptions','ls':
				[
					{'name':'fileA.css'},
					{'name':'fileF.txt'}
				]
			},
			{'path':'Magento/MageStarterKit','ls':
				[
					{'name':'fileA.css'},
					{'name':'fileB.phtml'},
					{'name':'fileC.php','tokens':
						[
						 	{'type':'filename','casing':'l','path':'path/to/file','name':'.'},
							{'type':'var','casing':'u','name':'some var'},
							{'type':'list','name':'some list'}
						]
					}
				]
			}
		],
		'hidden':
		[
			'Demos/_HiddenFolder',
			'Demos/_AnotherTemplate'
		]
	};
	return json;
}

function sample_json_templates2(){
	var json={'dirs':[{'path':'TestVarOptions','ls':[{'name':'fileB.txt','tokens':[{'type':'var','casing':'c','options':['Zebra'],'name':'animal'}]},{'name':'fileA.txt','tokens':[{'type':'var','casing':'c','name':'animal'}]},{'name':'fileC.txt','tokens':[{'type':'var','casing':'c','options':['Mouse','Tiger','Giraffe','Human'],'name':'animal'}]}]},{'path':'MageStarterKit','ls':[{'name':'app_code_codepool_prefix_module_helper_data.php','tokens':[{'type':'filename','casing':'c','dir':'app/code/[codepool]/[Prefix]/[Module]/Helper','name':'"Data"'},{'type':'var','casing':'n','name':'module prefix','alias':'[Prefix]'},{'type':'var','casing':'c','name':'module name','alias':'[Module]'},{'type':'var','casing':'l','name':'code pool','alias':'[codepool]'}]},{'name':'app_code_codepool_prefix_module_etc_adminhtml.xml','tokens':[{'type':'filename','casing':'l','dir':'app/code/[codepool]/[Prefix]/[Module]/etc','name':'"adminhtml"'},{'type':'var','casing':'n','name':'module prefix','alias':'[Prefix]'},{'type':'var','casing':'l','name':'module prefix','alias':'[prefix]'},{'type':'var','casing':'c','name':'module name','alias':'[Module]'},{'type':'var','casing':'l','name':'module name','alias':'[module]'},{'type':'var','casing':'l','name':'code pool','alias':'[codepool]'}]},{'name':'app_code_codepool_prefix_module_helper_samplehelper.php','tokens':[{'type':'filename','casing':'c','dir':'app/code/[codepool]/[Prefix]/[Module]/Helper','name':'"SampleHelper"'},{'type':'var','casing':'n','name':'module prefix','alias':'[Prefix]'},{'type':'var','casing':'c','name':'module name','alias':'[Module]'},{'type':'var','casing':'l','name':'code pool','alias':'[codepool]'}]},{'name':'app_design_frontend_layout_module.xml','tokens':[{'type':'filename','casing':'l','dir':'app/design/frontend/default/default/layout','name':'"[module]"'},{'type':'var','casing':'n','name':'module prefix','alias':'[Prefix]'},{'type':'var','casing':'l','name':'module prefix','alias':'[prefix]'},{'type':'var','casing':'c','name':'module name','alias':'[Module]'},{'type':'var','casing':'l','name':'module name','alias':'[module]'},{'type':'var','casing':'l','name':'code pool','alias':'[codepool]'}]},{'name':'app_etc_modules_prefix_module.xml','tokens':[{'type':'filename','casing':'l','dir':'app/etc/modules','name':'"[Prefix]_[Module]"'},{'type':'var','casing':'n','name':'module prefix','alias':'[Prefix]'},{'type':'var','casing':'l','name':'module prefix','alias':'[prefix]'},{'type':'var','casing':'c','name':'module name','alias':'[Module]'},{'type':'var','casing':'l','name':'module name','alias':'[module]'},{'type':'var','casing':'l','name':'code pool','alias':'[codepool]'},{'type':'var','casing':'n','name':'extension version'}]},{'name':'app_code_codepool_prefix_module_controllers_adminhtml_indexcontroller.php','tokens':[{'type':'filename','casing':'c','dir':'app/code/[codepool]/[Prefix]/[Module]/controllers/Adminhtml','name':'"IndexController"'},{'type':'var','casing':'n','name':'module prefix','alias':'[Prefix]'},{'type':'var','casing':'c','name':'module name','alias':'[Module]'},{'type':'var','casing':'l','name':'module prefix','alias':'[prefix]'},{'type':'var','casing':'l','name':'module name','alias':'[module]'},{'type':'var','casing':'l','name':'code pool','alias':'[codepool]'}]},{'name':'app_code_codepool_prefix_module_controllers_indexcontroller.php','tokens':[{'type':'filename','casing':'c','dir':'app/code/[codepool]/[Prefix]/[Module]/controllers','name':'"IndexController"'},{'type':'var','casing':'n','name':'module prefix','alias':'[Prefix]'},{'type':'var','casing':'c','name':'module name','alias':'[Module]'},{'type':'var','casing':'l','name':'module prefix','alias':'[prefix]'},{'type':'var','casing':'l','name':'module name','alias':'[module]'},{'type':'var','casing':'l','name':'code pool','alias':'[codepool]'}]},{'name':'app_design_adminhtml_layout_module.xml','tokens':[{'type':'filename','casing':'l','dir':'app/design/adminhtml/default/default/layout','name':'"[module]"'},{'type':'var','casing':'n','name':'module prefix','alias':'[Prefix]'},{'type':'var','casing':'l','name':'module prefix','alias':'[prefix]'},{'type':'var','casing':'c','name':'module name','alias':'[Module]'},{'type':'var','casing':'l','name':'module name','alias':'[module]'},{'type':'var','casing':'l','name':'code pool','alias':'[codepool]'}]},{'name':'app_code_codepool_prefix_module_block_adminhtml_system_config_fieldset_hint.php','tokens':[{'type':'filename','casing':'l','dir':'app/code/[codepool]/[Prefix]/[Module]/Block/Adminhtml/System/Config/Fieldset','name':'"Hint"'},{'type':'var','casing':'n','name':'module prefix','alias':'[Prefix]'},{'type':'var','casing':'l','name':'module prefix','alias':'[prefix]'},{'type':'var','casing':'c','name':'module name','alias':'[Module]'},{'type':'var','casing':'l','name':'module name','alias':'[module]'},{'type':'var','casing':'l','name':'code pool','alias':'[codepool]'}]},{'name':'app_code_codepool_prefix_module_etc_config.xml','tokens':[{'type':'filename','casing':'l','dir':'app/code/[codepool]/[Prefix]/[Module]/etc','name':'"config"'},{'type':'var','casing':'n','name':'module prefix','alias':'[Prefix]'},{'type':'var','casing':'l','name':'module prefix','alias':'[prefix]'},{'type':'var','casing':'c','name':'module name','alias':'[Module]'},{'type':'var','casing':'l','name':'module name','alias':'[module]'},{'type':'var','casing':'l','name':'code pool','alias':'[codepool]'},{'type':'var','casing':'n','name':'extension version'}]},{'name':'helogo.jpg'},{'name':'app_locale_enUS_prefx_module.csv','tokens':[{'type':'filename','casing':'l','dir':'app/locale/en_US','name':'"[Prefix]_[Module]"'},{'type':'var','casing':'n','name':'module prefix','alias':'[Prefix]'},{'type':'var','casing':'c','name':'module name','alias':'[Module]'}]},{'name':'app_design_frontend_template_module_example.phtml','tokens':[{'type':'filename','casing':'l','dir':'app/design/frontend/default/default/template/[module]','name':'"example"'},{'type':'var','casing':'n','name':'module prefix','alias':'[Prefix]'},{'type':'var','casing':'l','name':'module prefix','alias':'[prefix]'},{'type':'var','casing':'c','name':'module name','alias':'[Module]'},{'type':'var','casing':'l','name':'module name','alias':'[module]'},{'type':'var','casing':'l','name':'code pool','alias':'[codepool]'}]},{'name':'app_code_codepool_prefix_module_etc_system.xml','tokens':[{'type':'filename','casing':'l','dir':'app/code/[codepool]/[Prefix]/[Module]/etc','name':'"system"'},{'type':'var','casing':'n','name':'module prefix','alias':'[Prefix]'},{'type':'var','casing':'l','name':'module prefix','alias':'[prefix]'},{'type':'var','casing':'c','name':'module name','alias':'[Module]'},{'type':'var','casing':'l','name':'module name','alias':'[module]'},{'type':'var','casing':'l','name':'code pool','alias':'[codepool]'}]},{'name':'app_design_adminhtml_template_system_config_hint.phtml','tokens':[{'type':'filename','casing':'l','dir':'app/design/adminhtml/default/default/template/[module]/system/config/fieldset','name':'"hint"'},{'type':'var','casing':'n','name':'module prefix','alias':'[Prefix]'},{'type':'var','casing':'l','name':'module prefix','alias':'[prefix]'},{'type':'var','casing':'c','name':'module name','alias':'[Module]'},{'type':'var','casing':'l','name':'module name','alias':'[module]'},{'type':'var','casing':'l','name':'code pool','alias':'[codepool]'}]},{'name':'app_code_codepool_prefix_module_block_sampleblock.php','tokens':[{'type':'filename','casing':'c','dir':'app/code/[codepool]/[Prefix]/[Module]/Block','name':'"SampleBlock"'},{'type':'var','casing':'n','name':'module prefix','alias':'[Prefix]'},{'type':'var','casing':'c','name':'module name','alias':'[Module]'},{'type':'var','casing':'l','name':'module prefix','alias':'[prefix]'},{'type':'var','casing':'l','name':'module name','alias':'[module]'},{'type':'var','casing':'l','name':'code pool','alias':'[codepool]'}]},{'name':'app_code_codepool_prefix_module_model_samplemodel.php','tokens':[{'type':'filename','casing':'c','dir':'app/code/[codepool]/[Prefix]/[Module]/Model','name':'"SampleModel"'},{'type':'var','casing':'n','name':'module prefix','alias':'[Prefix]'},{'type':'var','casing':'l','name':'module prefix','alias':'[prefix]'},{'type':'var','casing':'c','name':'module name','alias':'[Module]'},{'type':'var','casing':'l','name':'module name','alias':'[module]'},{'type':'var','casing':'l','name':'code pool','alias':'[codepool]'}]},{'name':'app_code_codepool_prefix_module_model_system_config_source_example.php','tokens':[{'type':'filename','casing':'c','dir':'app/code/[codepool]/[Prefix]/[Module]/Model/System/Config/Source','name':'"Example"'},{'type':'var','casing':'n','name':'module prefix','alias':'[Prefix]'},{'type':'var','casing':'c','name':'module name','alias':'[Module]'},{'type':'var','casing':'l','name':'module prefix','alias':'[prefix]'},{'type':'var','casing':'l','name':'module name','alias':'[module]'},{'type':'var','casing':'l','name':'code pool','alias':'[codepool]'}]}],'hidden':['_helogo.jpg','_filenames.xml']},{'path':'TestInclude','ls':[{'name':'main.txt'}],'hidden':['_filenames.xml'],'includes':['test/*/allFiles','[css]/stuff*yep/sub.css','[css]/*include.css','[css]/stuff*/sub.css','[css]/*yep/sub.css','[css]/startswith*endswith.css','[css]/includethis.*','[css]/includethree.txt']},{'path':'ListTokenDemo','ls':[{'name':'createTablesScript.sql','tokens':[{'type':'var','casing':'n','name':'module qualifier','alias':'[ModQual]'},{'type':'list','name':'my tables'},{'type':'var','casing':'c','name':'table name','alias':'[Table]'},{'type':'list','name':'column names'},{'type':'var','casing':'l','name':'column'},{'type':'var','casing':'n','options':['int','datetime','varchar(150)','varchar(255)','varchar(510)','varchar(765)','nvarchar(max)'],'name':'data-type'}]}]}],'hidden':['TestHidden2/_SubHidden','_TestHidden']};
	return json;
}