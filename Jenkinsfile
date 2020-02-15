pipeline {
	agent any
	stages {
	    stage ('Clone') {
	    	steps {
	    		checkout scm
	    	}
	    }
	    stage('Prepare') {
	    	steps {
	        	sh 'mvn clean'
	    	}
	    }
	    stage('Build') {
	    	steps {
	        	sh 'mvn install'
	    	}
	    }
	    stage('Test') {
	    	steps {
	        	sh 'mvn test'
	    	}
	    }
	    stage('Deploy') {
	    	steps {
	        	archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
	    	}
	    }
    }
}
