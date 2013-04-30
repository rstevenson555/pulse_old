$(function(){

				// Datepicker
				$('#date').datepicker({
					inline: true
					//dateFormat: 'dd-mm-yy'
				});
				$('#date2').datepicker({
					inline: true
					//dateFormat: 'yy-mm-dd'
				});
				//hover states on the static widgets
				$('#dialog_link, ul#icons li').hover(
					function() { $(this).addClass('ui-state-hover'); }, 
					function() { $(this).removeClass('ui-state-hover'); }
				);
				
				$('#timepicker').timepicker();
				$('#timepicker2').timepicker();
				
				var dateFormat = $("#date").datepicker( "option", "dateFormat" );
				$("#date").datepicker("option", "dateFormat", 'dd-M-y');
				var dateFormat2 = $("#date2").datepicker( "option", "dateFormat" );
				$("#date2").datepicker("option", "dateFormat", 'dd-M-y');
				
			});
			
